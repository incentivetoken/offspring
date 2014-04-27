package com.dgex.offspring.providers.service;

public interface ICurrency {

  /* Fiat Currencies */

  public static final String AUD = "AUD";
  public static final String BRL = "BRL";
  public static final String CAD = "CAD";
  public static final String CHF = "CHF";
  public static final String CNY = "CNY";
  public static final String EUR = "EUR";
  public static final String GBP = "GBP";
  public static final String ILS = "ILS";
  public static final String JPY = "JPY";
  public static final String NOK = "NOK";
  public static final String NZD = "NZD";
  public static final String PLN = "PLN";
  public static final String RUB = "RUB";
  public static final String SEK = "SEK";
  public static final String SGD = "SGD";
  public static final String TRY = "TRY";
  public static final String USD = "USD";
  public static final String ZAR = "ZAR";
  public static final String RON = "RON";
  public static final String HKD = "HKD";
  public static final String MXN = "MXN";

  /* Crypto Currencies */

  public static final String BTC = "BTC";
  public static final String LTC = "LTC";
  public static final String PPC = "PPC";
  public static final String DODGE = "DODGE";
  public static final String NXT = "NXT";
  public static final String MSC = "MSC";
  public static final String NMC = "NMC";
  public static final String QRK = "QRK";
  public static final String XPM = "XPM";


  public String getId();

  public String getDescription();

  public String getLabel();

  public String getURL();

  public String getDecimalFormat();

  public ICurrencyPair getPair(ICurrency quote);
}
