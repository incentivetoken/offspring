package com.dgex.offspring.nxtCore.service;

import java.math.BigInteger;

import nxt.Constants;

public class Utils {

  private static final long[] multipliers = { 1, 10, 100, 1000, 10000, 100000,
      1000000, 10000000, 100000000 };

  public static Long getAmountNQT(String quantString) {
    Long amountNQT = parseQNT(quantString, 8);
    if (amountNQT == null) {
      return null;
    }
    if (amountNQT <= 0 || amountNQT >= Constants.MAX_BALANCE_NQT) {
      return null;
    }
    return amountNQT;
  }

  public static Long getFeeNQT(String quantString) {
    Long feeNQT = parseQNT(quantString, 8);
    if (feeNQT == null) {
      return null;
    }
    if (feeNQT <= 0 || feeNQT >= Constants.MAX_BALANCE_NQT) {
      return null;
    }
    return feeNQT;
  }

  // public static Long getQuantityQNT(String quantString) {
  // Long quantityQNT = parseQNT(quantString);
  // if (quantityQNT == null) {
  // return null;
  // }
  // if (quantityQNT <= 0 || quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT) {
  // return null;
  // }
  // return quantityQNT;
  // }

  public static Long getQuantityQNT(String quantString, int decimals) {
    Long quantityQNT = parseQNT(quantString, decimals);
    if (quantityQNT == null) {
      return null;
    }
    if (quantityQNT <= 0 || quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT) {
      return null;
    }
    return quantityQNT;
  }

  // public static String quantToString(Long quant) {
  // if (quant == null) {
  // return "";
  // }
  // return toStringFraction(quant, 8);
  // }

  public static String quantToString(Long quant, int decimals) {
    if (quant == null) {
      return "";
    }
    return toStringFraction(quant, decimals);
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
    String result = buf.toString();
    return result.replaceAll("0+$", "");
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
  // public static Long parseQNT(String quantString) {
  // try {
  // Long quant = parseStringFraction(quantString, 8,
  // Constants.MAX_BALANCE_NXT);
  // return quant;
  // }
  // catch (Exception e) {
  // return null;
  // }
  // }

  public static Long parseQNT(String quantString, int decimals) {
    try {
      Long quant = parseStringFraction(quantString, decimals,
          Constants.MAX_BALANCE_NXT);
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

  private static long parseStringFraction(String value, int decimals,
      long maxValue) {
    String[] s = value.trim().split("\\.");
    if (s.length == 0 || s.length > 2) {
      throw new NumberFormatException("Invalid number: " + value);
    }
    long wholePart = Long.parseLong(s[0]);
    if (wholePart > maxValue) {
      throw new IllegalArgumentException(
          "Whole part of value exceeds maximum possible");
    }
    if (s.length == 1) {
      return wholePart * multipliers[decimals];
    }
    long fractionalPart = Long.parseLong(s[1]);
    if (fractionalPart >= multipliers[decimals] || s[1].length() > decimals) {
      throw new IllegalArgumentException(
          "Fractional part exceeds maximum allowed divisibility");
    }
    for (int i = s[1].length(); i < decimals; i++) {
      fractionalPart *= 10;
    }
    return wholePart * multipliers[decimals] + fractionalPart;
  }

  public static Double quantToDouble(long quant, int decimals) {
    String string = quantToString(quant, decimals);
    if (string != null) {
      try {
        return Double.parseDouble(string);
      }
      catch (ArithmeticException e) {
      }
    }
    return Double.valueOf(0);
  }
  
//  var priceNQT = new BigInteger(String($tr.data("price")));
//  var quantityQNT = new BigInteger(String($tr.data("quantity")));
//  var totalNQT = new BigInteger(NRS.calculateOrderTotalNQT(quantityQNT, priceNQT));
//
//  $("#" + type + "_asset_price").val(NRS.calculateOrderPricePerWholeQNT(priceNQT, NRS.currentAsset.decimals));
//  $("#" + type + "_asset_quantity").val(NRS.convertToQNTf(quantityQNT, NRS.currentAsset.decimals));
//  $("#" + type + "_asset_total").val(NRS.convertToNXT(totalNQT));

  public static long calculateOrderPricePerWholeQNT_InNQT(long priceNQT, int decimals) {
    return BigInteger.valueOf(priceNQT).multiply(BigInteger.valueOf(Double.valueOf(Math.pow(10, decimals)).longValue())).longValue();
  }
  
//  public String convertToQNTf(long quantityQNT, int decimals) {
//    return quantToString(quantityQNT, decimals);
//  }
  
  public static long calculateOrderTotalNQT(long quantityQNT, long priceNQT) {
    return BigInteger.valueOf(quantityQNT).multiply(BigInteger.valueOf(priceNQT)).longValue();
  }
  

  
  
  
//  NRS.convertToQNTf = function(quantity, decimals, returnAsObject) {
//    quantity = String(quantity);
//
//    if (quantity.length < decimals) {
//      for (var i = quantity.length; i < decimals; i++) {
//        quantity = "0" + quantity;
//      }
//    }
//
//    var afterComma = "";
//
//    if (decimals) {
//      afterComma = "." + quantity.substring(quantity.length - decimals);
//      quantity = quantity.substring(0, quantity.length - decimals);
//
//      if (!quantity) {
//        quantity = "0";
//      }
//
//      afterComma = afterComma.replace(/0+$/, "");
//
//      if (afterComma == ".") {
//        afterComma = "";
//      }
//    }
//
//    if (returnAsObject) {
//      return {
//        "amount": quantity,
//        "afterComma": afterComma
//      };
//    } else {
//      return quantity + afterComma;
//    }
//  }
  
  //
  // NRS.calculateOrderTotalNQT = function(quantityQNT, priceNQT) {
  // if (typeof quantityQNT != "object") {
  // quantityQNT = new BigInteger(String(quantityQNT));
  // }
  //
  // if (typeof priceNQT != "object") {
  // priceNQT = new BigInteger(String(priceNQT));
  // }
  //
  // var orderTotal = quantityQNT.multiply(priceNQT);
  //
  // return orderTotal.toString();
  // }
}
