package com.dgex.offspring.ui.messaging;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import nxt.crypto.Crypto;
import nxt.crypto.MyCurve25519;
import nxt.crypto.XoredData;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import com.dgex.offspring.config.Config;

public class MessageCrypto {

  // About Using SHA1 for SecureRandom..
  // http://csrc.nist.gov/publications/nistpubs/800-131A/sp800-131A.pdf
  //
  // ...
  // From January 1, 2011 through December 31, 2013, the use of SHA-1 is
  // deprecated for digital signature generation. The user must accept risk when
  // SHA-1 is used, particularly when approaching the December 31, 2013 upper
  // limit.
  //
  // ...
  // For all other hash function applications, the use of SHA-1 is acceptable.
  // The other applications include HMAC, Key Derivation Functions (KDFs),
  // Random Number Generation (RNGs and RBGs), and hash-only applications (e.g.,
  // hashing passwords and using SHA-1 to compute a checksum, such as the
  // approved integrity technique specified in Section 4.6.1 of [FIPS 140-2]).

  static Logger logger = Logger.getLogger(MessageCrypto.class);

  // public static boolean test(String secretPhrase, byte[] theirPublicKey)
  // throws UnsupportedEncodingException {
  //
  // String secretMessage = "Hello world";
  // logger.info("secretPhrase=" + secretPhrase);
  // logger.info("secretMessage=" + secretMessage);
  // logger.info("theirPublicKey=" + theirPublicKey);
  //
  // byte[] ciphertext;
  // try {
  // ciphertext = encrypt(secretMessage, secretPhrase, theirPublicKey);
  // logger.info("ciphertext=" + ciphertext);
  // }
  // catch (UnsupportedEncodingException e) {
  // e.printStackTrace();
  // return false;
  // }
  //
  // if (!startsWithMagicByte(ciphertext)) {
  // logger.info("Does not start with magic byte");
  // return false;
  // }
  //
  // String clearText;
  // try {
  // clearText = decrypt(ciphertext, secretPhrase, theirPublicKey);
  // logger.info("clearText=" + clearText);
  // }
  // catch (UnsupportedEncodingException e) {
  // e.printStackTrace();
  // return false;
  // }
  //
  // boolean match = clearText.equals(secretMessage);
  // logger.info("match=" + match);
  // return match;
  // }

  /**
   * Encrypt a clear text string for use in a message.
   * 
   * @param plaintext
   * @param secretPhrase
   *          String
   * @param theirPublicKey
   * @return
   * @throws UnsupportedEncodingException
   */

  /* No new messages should be encrypted with XoredData */

  // public static byte[] encrypt(String plaintext, String secretPhrase,
  // byte[] theirPublicKey) throws UnsupportedEncodingException {
  //
  // byte[] bytes = plaintext.getBytes("UTF-8");
  // byte[] myPrivateKey = MyCurve25519.getPrivateKey(secretPhrase);
  //
  // XoredData xored = XoredData.encrypt(bytes, myPrivateKey, theirPublicKey);
  //
  // byte[] magic = Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER_XOR;
  // byte[] nonce = xored.getNonce();
  // byte[] data = xored.getData();
  // byte[] message = new byte[magic.length + nonce.length + data.length];
  //
  // System.arraycopy(magic, 0, message, 0, magic.length);
  // System.arraycopy(nonce, 0, message, magic.length, nonce.length);
  // System
  // .arraycopy(data, 0, message, nonce.length + magic.length, data.length);
  //
  // return message;
  // }
  
  /**
   * Encrypt a clear text smagictring for use in a message.
   * 
   * @param plaintext
   * @param secretPhrase
   *          String
   * @param theirPublicKey
   * @return
   * @throws GeneralSecurityException
   * @throws IOException
   * @throws InvalidCipherTextException
   * @throws IllegalStateException
   * @throws DataLengthException
   */
  public static byte[] encryptAES(String plaintext, String secretPhrase,
      byte[] theirPublicKey) throws GeneralSecurityException, IOException,
      DataLengthException, IllegalStateException, InvalidCipherTextException {

    byte[] myPrivateKey = MyCurve25519.getPrivateKey(secretPhrase);

    byte[] dhSharedSecret = new byte[32];
    MyCurve25519.curve(dhSharedSecret, myPrivateKey, theirPublicKey);
    byte[] key = Crypto.sha256().digest(dhSharedSecret);

    byte[] iv = new byte[16];
    SecureRandom.getInstance("SHA1PRNG", "SUN").nextBytes(iv);

    PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(
        new CBCBlockCipher(new AESEngine()));

    CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
    aes.init(true, ivAndKey);

    byte[] plainTextBytes = plaintext.getBytes("UTF-8");

    byte[] output = new byte[aes.getOutputSize(plainTextBytes.length)];
    int len = aes.processBytes(plainTextBytes, 0, plainTextBytes.length,
        output, 0);
    aes.doFinal(output, len);

    byte[] magic = Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER_AES;
    byte[] message = new byte[magic.length + iv.length + output.length];

    System.arraycopy(magic, 0, message, 0, magic.length);
    System.arraycopy(iv, 0, message, magic.length, iv.length);
    System.arraycopy(output, 0, message, iv.length + magic.length,
        output.length);

    return message;
  }

  /**
   * Decrypts an encrypted string
   * 
   * @param bytes
   * @param secretPhrase
   * @param theirPublicKey
   * @return
   * @throws UnsupportedEncodingException
   * @throws GeneralSecurityException
   * @throws InvalidCipherTextException
   * @throws IllegalStateException
   * @throws DataLengthException
   */
  public static String decrypt(byte[] bytes, String secretPhrase,
      byte[] theirPublicKey) throws UnsupportedEncodingException,
      GeneralSecurityException, DataLengthException, IllegalStateException,
      InvalidCipherTextException {

    /* XOR encrypted */
    if (startsWith(bytes, Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER_XOR)) {
      return decryptXOR(bytes, secretPhrase, theirPublicKey);
    }

    /* AES encrypted messages */
    else if (startsWith(bytes, Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER_AES)) {
      return decryptAES(bytes, secretPhrase, theirPublicKey);
    }

    throw new RuntimeException(
        "Ciphertext does not start with a supported magic number");
  }

  public static String decryptXOR(byte[] bytes, String secretPhrase,
      byte[] theirPublicKey) throws UnsupportedEncodingException {

    byte[] mykey = MyCurve25519.getPrivateKey(secretPhrase);
    byte[] magic = new byte[Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER_XOR.length];
    byte[] nonce = new byte[32];
    byte[] data = new byte[bytes.length - magic.length - nonce.length];
    
    getBytes(bytes, 0, magic.length, magic, 0);
    getBytes(bytes, magic.length, magic.length + nonce.length, nonce, 0);
    getBytes(bytes, magic.length + nonce.length, magic.length + nonce.length
        + data.length, data, 0);
    
    XoredData xored = new XoredData(data, nonce);
    byte[] plainText = xored.decrypt(mykey, theirPublicKey);
    return new String(plainText, "UTF-8");
  }
  
  /**
   * Decrypts an encrypted string
   * 
   * @param bytes
   * @param secretPhrase
   * @param theirPublicKey
   * @return
   * @throws UnsupportedEncodingException
   * @throws GeneralSecurityException
   * @throws InvalidCipherTextException
   * @throws IllegalStateException
   * @throws DataLengthException
   */
  public static String decryptAES(byte[] bytes, String secretPhrase,
      byte[] theirPublicKey) throws GeneralSecurityException,
      UnsupportedEncodingException, DataLengthException, IllegalStateException,
      InvalidCipherTextException {

    if (!startsWith(bytes, Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER_AES))
      throw new RuntimeException("Ciphertext does not start with magic number");

    byte[] magic = new byte[Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER_AES.length];
    byte[] iv = new byte[16];
    byte[] ciphertext = new byte[bytes.length - magic.length - iv.length];

    if (ciphertext.length <= 0)
      throw new RuntimeException("Message length invalid");

    getBytes(bytes, 0, magic.length, magic, 0);
    getBytes(bytes, magic.length, magic.length + iv.length, iv, 0);
    getBytes(bytes, magic.length + iv.length, magic.length + iv.length
        + ciphertext.length, ciphertext, 0);

    byte[] mykey = MyCurve25519.getPrivateKey(secretPhrase);
    byte[] dhSharedSecret = new byte[32];
    MyCurve25519.curve(dhSharedSecret, mykey, theirPublicKey);

    byte[] key = Crypto.sha256().digest(dhSharedSecret);

    PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(
        new CBCBlockCipher(new AESEngine()));

    CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);
    aes.init(false, ivAndKey);

    byte[] plainText = new byte[aes.getOutputSize(ciphertext.length)];
    int len = aes.processBytes(ciphertext, 0, ciphertext.length, plainText, 0);
    aes.doFinal(plainText, len);

    return new String(plainText, "UTF-8");
  }

  public static boolean startsWithMagicEncryptedByte(byte[] bytes) {
    return startsWith(bytes, Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER_XOR)
        || startsWith(bytes, Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER_AES);
  }

  public static boolean startsWithMagicUnEncryptedByte(byte[] bytes) {
    return startsWith(bytes, Config.MAGIC_UNENCRYPTED_MESSAGE_NUMBER);
  }

  private static boolean startsWith(byte[] source, byte[] match) {
    return startsWith(source, 0, match);
  }

  private static boolean startsWith(byte[] source, int offset, byte[] match) {
    if (match.length > (source.length - offset)) {
      return false;
    }
    for (int i = 0; i < match.length; i++) {
      if (source[offset + i] != match[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Copies bytes from the source byte array to the destination array
   * 
   * @param source
   *          The source array
   * @param srcBegin
   *          Index of the first source byte to copy
   * @param srcEnd
   *          Index after the last source byte to copy
   * @param destination
   *          The destination array
   * @param dstBegin
   *          The starting offset in the destination array
   */
  private static void getBytes(byte[] source, int srcBegin, int srcEnd,
      byte[] destination, int dstBegin) {
    System
        .arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
  }
}
