package nxt.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import nxt.util.Logger;

public class MyCurve25519 {

  public static byte[] getPrivateKey(String secretPhrase) {
    try {
      MessageDigest digest = Crypto.sha256();
      byte[] s = digest.digest(secretPhrase.getBytes("UTF-8"));
      Curve25519.clamp(s);
      return s;
    }
    catch (RuntimeException | UnsupportedEncodingException e) {
      Logger.logMessage("Error getting private key", e);
      return null;
    }
  }
}
