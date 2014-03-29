package com.dgex.offspring.ui.controls;

import java.io.UnsupportedEncodingException;

import nxt.crypto.MyCurve25519;
import nxt.crypto.XoredData;

import org.apache.log4j.Logger;

import com.dgex.offspring.config.Config;

public class MessageCrypto {

  static Logger logger = Logger.getLogger(MessageCrypto.class);

  public static boolean test(String secretPhrase, byte[] theirPublicKey)
      throws UnsupportedEncodingException {

    String secretMessage = "Hello world";
    logger.info("secretPhrase=" + secretPhrase);
    logger.info("secretMessage=" + secretMessage);
    logger.info("theirPublicKey=" + theirPublicKey);

    // byte[] myPrivateKey = Crypto.sha256()
    // .digest(secretPhrase.getBytes("UTF-8"));
    // XoredData xored = XoredData.encrypt(secretMessage.getBytes("UTF-8"),
    // myPrivateKey, theirPublicKey);
    //
    // logger.info("ciphertext=" + xored.getData());
    //
    // xored = new XoredData(xored.getData(), xored.getNonce());
    //
    // String clearText = new String(xored.decrypt(myPrivateKey,
    // theirPublicKey),
    // "UTF-8");
    // logger.info("clearText=" + clearText);
    //
    // boolean match = clearText.equals(secretMessage);
    // logger.info("match=" + match);
    // return match;

    byte[] ciphertext;
    try {
      ciphertext = encrypt(secretMessage, secretPhrase, theirPublicKey);
      logger.info("ciphertext=" + ciphertext);
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return false;
    }

    if (!startsWithMagicByte(ciphertext)) {
      logger.info("Does not start with magic byte");
      return false;
    }

    String clearText;
    try {
      clearText = decrypt(ciphertext, secretPhrase, theirPublicKey);
      logger.info("clearText=" + clearText);
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return false;
    }

    boolean match = clearText.equals(secretMessage);
    logger.info("match=" + match);
    return match;
  }

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
  public static byte[] encrypt(String plaintext, String secretPhrase,
      byte[] theirPublicKey) throws UnsupportedEncodingException {
    
    byte[] bytes = plaintext.getBytes("UTF-8");
    byte[] myPrivateKey = MyCurve25519.getPrivateKey(secretPhrase);
    
    XoredData xored = XoredData.encrypt(bytes, myPrivateKey, theirPublicKey);

    byte[] magic = Config.MAGIC_ENCRYPTION_NUMBER;
    byte[] nonce = xored.getNonce();
    byte[] data = xored.getData();
    byte[] message = new byte[magic.length + nonce.length + data.length];

    System.arraycopy(magic, 0, message, 0, magic.length);
    System.arraycopy(nonce, 0, message, magic.length, nonce.length);
    System
        .arraycopy(data, 0, message, nonce.length + magic.length, data.length);

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
   */
  public static String decrypt(byte[] bytes, String secretPhrase,
      byte[] theirPublicKey) throws UnsupportedEncodingException {
    
    byte[] mykey = MyCurve25519.getPrivateKey(secretPhrase);
    byte[] magic = new byte[Config.MAGIC_ENCRYPTION_NUMBER.length];
    byte[] nonce = new byte[32];
    byte[] data = new byte[bytes.length - magic.length - nonce.length];
    
    if (!startsWith(bytes, Config.MAGIC_ENCRYPTION_NUMBER))
      throw new RuntimeException("Ciphertext does not start with magic number");
    
    getBytes(bytes, 0, magic.length, magic, 0);
    getBytes(bytes, magic.length, magic.length + nonce.length, nonce, 0);
    getBytes(bytes, magic.length + nonce.length, magic.length + nonce.length
        + data.length, data, 0);
    
    XoredData xored = new XoredData(data, nonce);
    byte[] plainText = xored.decrypt(mykey, theirPublicKey);
    return new String(plainText, "UTF-8");
  }
  
  public static boolean startsWithMagicByte(byte[] bytes) {
    return startsWith(bytes, Config.MAGIC_ENCRYPTION_NUMBER);
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
