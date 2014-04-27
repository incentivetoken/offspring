package com.dgex.offspring.nxtCore.service;

import nxt.Constants;
import nxt.util.Convert;

public class Utils {

  private static final long[] multipliers = { 1, 10, 100, 1000, 10000, 100000,
      1000000, 10000000, 100000000 };

  public static Long getAmountNQT(String quantString) {
    Long amountNQT = parseQNT(quantString);
    if (amountNQT == null) {
      return null;
    }
    if (amountNQT <= 0 || amountNQT >= Constants.MAX_BALANCE_NQT) {
      return null;
    }
    return amountNQT;
  }

  public static Long getFeeNQT(String quantString) {
    Long feeNQT = parseQNT(quantString);
    if (feeNQT == null) {
      return null;
    }
    if (feeNQT <= 0 || feeNQT >= Constants.MAX_BALANCE_NQT) {
      return null;
    }
    return feeNQT;
  }

  public static Long getQuantityQNT(String quantString) {
    Long quantityQNT = parseQNT(quantString);
    if (quantityQNT == null) {
      return null;
    }
    if (quantityQNT <= 0 || quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT) {
      return null;
    }
    return quantityQNT;
  }

  public static String quantToString(Long quant) {
    if (quant == null) {
      return "";
    }
    return toStringFraction(quant, 8);
  }

  private static String toStringFraction(long number, int decimals) {
    boolean negative = false;
    if (number < 0) {
      negative = true;
      number = -number;
    }
    long wholePart = number / multipliers[decimals];
    long fractionalPart = number % multipliers[decimals];
    if (fractionalPart == 0) {
      return String.valueOf(wholePart);
    }
    StringBuilder buf = new StringBuilder();
    if (negative) {
      buf.append('-');
    }
    buf.append(wholePart);
    buf.append('.');
    String fractionalPartString = String.valueOf(fractionalPart);
    for (int i = fractionalPartString.length(); i < decimals; i++) {
      buf.append('0');
    }
    buf.append(fractionalPartString);
    return buf.toString();
  }

  /**
   * Parses anything upto 8 decimals behind the comma. Example:
   * 
   * parseQNT("10000.00000001") == 1000000000001 
   * parseQNT("10000.01      ") == 1000001000000
   * 
   * @param quantString
   * @return
   */
  public static Long parseQNT(String quantString) {
    try {
      Long quant = Convert.parseNXT(quantString);
      return quant;
    }
    catch (Exception e) {
      return null;
    }
  }

  public static Byte parseDecimals(String byteString) {
    try {
      byte decimals = Byte.parseByte(byteString);
      if (decimals < 0 || decimals > 8) {
        return null;
      }
      return Byte.valueOf(decimals);
    }
    catch (Exception e) {
      return null;
    }
  }
}
