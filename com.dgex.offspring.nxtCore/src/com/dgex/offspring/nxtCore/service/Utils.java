package com.dgex.offspring.nxtCore.service;

import nxt.Constants;
import nxt.util.Convert;

import org.apache.log4j.Logger;

public class Utils {
  
  static final Logger logger = Logger.getLogger(Utils.class);

  private static final long[] multipliers = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };

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

  public static Long parseQNT(String quantString, int decimals) {
    try {
      Long quant = parseStringFraction(quantString, decimals, Constants.MAX_BALANCE_NXT);
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

  // Fractional asset quantities. Enjoy another reset of testnet.
  //
  // When issuing an asset, the maximum allowed number of digits after the asset
  // quantity decimal point can be specified as "decimals" parameter (allowed
  // values 0 to 8). For example, this would be 2 for a currency like EUR, 8 for Bitcoin.
  //
  // Similar to prices, asset quantities can be specified either as quantityQNT,
  // expressed in the smallest possible quantity unit ("quant") for the specific
  // asset, or as quantityINT, expressed in whole asset units (which may however
  // contain a fractional part despite its name).
  // For example, 9.97 USD can be expressed as either quantityINT="9.97", or
  // quantityQNT="997", assuming decimals=2 for USD.
  //
  // JSON responses containing quantities or asset balances are again returned in
  // both quantityINT and quantityQNT, as strings.
  //
  // When placing an ask or bid order, if quantityQNT is specified, the price is
  // interpreted to apply to QNT quantity units (regardless of whether the price
  // itself was specified in NXT or in NQT). If quantityINT is specified, the
  // price is interpreted to apply to INT quantity units.
  // For example, a bid order for quantityQNT="300", priceNXT="2", for asset USD
  // will be interpreted as $3.00 at 1 cent = 2 NXT, i.e. 200 NXT for one
  // dollar, 600 NXT total.
  // If parameters are submitted as quantityINT="7", priceNXT="50", the order will
  // be for $7.00 at 50 NXT for one dollar, 350 NXT total.
  //
  // Internally order matching and tracking of account asset balances is always
  // done in quantityQNT. An unfortunate side effect for now is that when
  // placing an order using quantityINT, the calculated price in NQT for one QNT unit
  // must be an integer.
  // So you cannot place an order for quantityINT="1", priceNXT="10.12345678"
  // (or equivalently priceNQT="1012345678"), because then the price of one
  // quantityQNT will be 10123456.78 NQT. The priceNXT will have to be either
  // 10.12345600 or 10.12345700. If this is confusing, specify the order as
  // quantityQNT="100", priceNQT="10123456".

  private static long pow10(int a) {
    long result = 1;
    for (int i = 0; i < a; i++) {
      result *= 10;
    }
    return result;
  }

  /**
   * User enters the price in the buy/sell price box, this price is then parsed
   * into {priceNQT}, this price is for one whole quant, we want to convert that
   * to the price in NQT for the smallest possible QNT.
   * 
   * @param priceNQT
   * @param quantityQNT
   * @param decimals
   * @return
   */
  public static long getPricePerQuantityQNT(long priceNQT, int decimals) throws ArithmeticException {
    if (priceNQT > 0) {
      long pow = pow10(decimals);
      try {
        return Convert.safeDivide(priceNQT, pow);
      }
      catch (ArithmeticException e) {
        logger.error("ArithmeticException", e);
      }
    }
    return 0l;
  }  
  
  /**
   * Only so many decimals are allowed for an order based on the number of
   * decimals defined on the asset. The number of decimals is limited to 8 minus
   * the number of decimals on the asset.
   * 
   * @param text
   * @param decimals
   * @return
   */
  public static boolean validatePriceDecimalsForOrder(String text, int decimals) {
    try {
      parseStringFraction(text, 8 - decimals, Constants.MAX_BALANCE_NXT);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * Validates that a quantity obbeys the maximum allowed number of decimal
   * places. Returns false in case there are to many or the value is to high.
   * 
   * @param text
   * @param decimals
   * @return
   */
  public static boolean vaildateQuantityDecimalsForOrder(String text, int decimals) {
    try {
      parseStringFraction(text, decimals, Constants.MAX_BALANCE_NXT);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * User enters the price in the buy/sell price box, this price is then parsed
   * into {priceNQTperQNT}, this price is for one smallest quant. User enters
   * the quantity in the buy/sell quantity box, this is in whole quant and is
   * parsed in smallest QNT through {stringToQuantityQNT}. Order total in NQT is
   * the {priceNQTperQNT} times {quantityQNT}.
   * 
   * @param priceNQTperWholeQNT
   * @param quantityQNT
   * @param decimals
   * @return
   */
  public static long calculateOrderTotalNQT(long priceNQTperQNT, long quantityQNT) throws ArithmeticException {
    try {
      return Convert.safeMultiply(priceNQTperQNT, quantityQNT);
    }
    catch (ArithmeticException e) {
      logger.error("ArithmeticException", e);
    }
    return 0l;
  }
  
  /**
   * Use for displaying order price in whole QNT. Order price is in NQT per smallest
   * QNT, this method translates NQT per smallest QNT to NQT per whole quant.
   * 
   * @param priceNQTperQNT
   * @param decimals
   * @return
   */
  public static long calculatePriceNQTperWholeQNT(long priceNQTperQNT, int decimals) {
    if (priceNQTperQNT > 0) {
      long pow = pow10(decimals);
      try {
        return Convert.safeMultiply(priceNQTperQNT, pow);
      } catch (ArithmeticException e) {
        logger.error("ArithmeticException", e);
      }
    }
    return 0l;
  }

}
