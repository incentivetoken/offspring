package com.dgex.offspring.providers.service;

import java.util.HashMap;
import java.util.Map;

public class Currencies {

  private static final Map<String, ICurrency> currencies = new HashMap<String, ICurrency>();

  private static final Map<String, ICurrencyPair> currencyPairs = new HashMap<String, ICurrencyPair>();

  public static ICurrency getCurrency(String id) {
    return currencies.get(id);
  }

  public static ICurrencyPair getCurrencyPair(ICurrency base, ICurrency quote) {
    String key = base.getId() + quote.getId();
    if (!(currencyPairs.containsKey(key))) {
      ICurrencyPair pair = new CurrencyPair(base, quote);
      currencyPairs.put(key, pair);
      return pair;
    }
    return currencyPairs.get(key);
  }

  public static class CurrencyPair implements ICurrencyPair {

    private final ICurrency base;
    private final ICurrency quote;

    public CurrencyPair(ICurrency base, ICurrency quote) {
      this.base = base;
      this.quote = quote;
    }

    @Override
    public ICurrency getBase() {
      return base;
    }

    @Override
    public ICurrency getQuote() {
      return quote;
    }

    @Override
    public String toString() {
      return "CurrencyPair[" + base.getId() + "/" + quote.getId() + "]";
    }
  };

  public static class Currency implements ICurrency {

    private final String id;
    private final String description;
    private final String label;
    private final String url;
    private final String format;

    public Currency(String id, String format, String description, String label,
        String url) {
      this.id = id;
      this.format = format;
      this.description = description;
      this.label = label;
      this.url = url;
      currencies.put(id, this);
    }

    @Override
    public String toString() {
      return "Currency[" + id + "]";
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public String getLabel() {
      return label;
    }

    @Override
    public String getURL() {
      return url;
    }

    @Override
    public String getDecimalFormat() {
      return format;
    }

    @Override
    public ICurrencyPair getPair(ICurrency quote) {
      return getCurrencyPair(this, quote);
    }
  };

  /* Fiat Currencies */

  public static ICurrency AUD = new Currency(ICurrency.AUD, "##.00", "AUD",
      "Australian Dollar", "");
  public static ICurrency BRL = new Currency(ICurrency.BRL, "##.00", "BRL",
      "Brazilian Real", "");
  public static ICurrency CAD = new Currency(ICurrency.CAD, "##.00", "CAD",
      "Canadian Dollar", "");
  public static ICurrency CHF = new Currency(ICurrency.CHF, "00.00", "CHF",
      "Swiss Franc", "");
  public static ICurrency CNY = new Currency(ICurrency.CNY, "00.00", "CNY",
      "Yuan", "");
  public static ICurrency EUR = new Currency(ICurrency.EUR, "##.00", "EUR",
      "Euro", "");
  public static ICurrency GBP = new Currency(ICurrency.GBP, "00.00", "GBP",
      "British Pond", "");
  public static ICurrency ILS = new Currency(ICurrency.ILS, "00.00", "ILS",
      "Israeli Shekel", "");
  public static ICurrency JPY = new Currency(ICurrency.JPY, "00.00", "JPY",
      "Yen", "");
  public static ICurrency NOK = new Currency(ICurrency.NOK, "00.00", "NOK",
      "Norwegian Krone", "");
  public static ICurrency NZD = new Currency(ICurrency.NZD, "00.00", "NZD",
      "New Zealand Dollar", "");
  public static ICurrency PLN = new Currency(ICurrency.PLN, "00.00", "PLN",
      "Polish Złoty", "");
  public static ICurrency RUB = new Currency(ICurrency.RUB, "00.00", "RUB",
      "Ruble", "");
  public static ICurrency SEK = new Currency(ICurrency.SEK, "00.00", "SEK",
      "Swedish Krona", "");
  public static ICurrency SGD = new Currency(ICurrency.SGD, "00.00", "SGD",
      "Singapore Dollar", "");
  public static ICurrency TRY = new Currency(ICurrency.TRY, "00.00", "TRY",
      "Turkish Lira", "");
  public static ICurrency USD = new Currency(ICurrency.USD, "00.00", "USD",
      "Dollar", "");
  public static ICurrency ZAR = new Currency(ICurrency.ZAR, "00.00", "ZAR",
      "South African Rand", "");

  /* Crypto Currencies */

  public static ICurrency BTC = new Currency(ICurrency.BTC, "00.00", "Bitcoin",
      "Bitcoin", "http://bitcoin.org");
  public static ICurrency LTC = new Currency(ICurrency.LTC, "00.00",
      "Litecoin", "Litecoin", "http://litecoin.org");
  public static ICurrency PPC = new Currency(ICurrency.PPC, "00.00",
      "Peercoin", "Peercoin", "http://peercoin.net");
  public static ICurrency DODGE = new Currency(ICurrency.DODGE, "00.00",
      "Dodgecoin", "Dodgecoin", "http://dodgecoin.com");
  public static ICurrency NXT = new Currency(ICurrency.NXT, "##0.00000000",
      "NXT", "NXT", "http://nxtcrypto.org");
  public static ICurrency MSC = new Currency(ICurrency.MSC, "00.00", "MSC",
      "Mastercoin", "http://www.mastercoin.org/");
  public static ICurrency NMC = new Currency(ICurrency.NMC, "00.00", "NMC",
      "Namecoin", "http://namecoin.info/");
  public static ICurrency QRK = new Currency(ICurrency.QRK, "00.00", "QRK",
      "Quark", "http://qrk.cc/");
  public static ICurrency XPM = new Currency(ICurrency.XPM, "00.00", "XPM",
      "Primecoin", "http://primecoin.io/");

}
