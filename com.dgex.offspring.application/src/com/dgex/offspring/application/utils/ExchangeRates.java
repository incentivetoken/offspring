package com.dgex.offspring.application.utils;

import java.util.List;

import org.apache.log4j.Logger;

import com.dgex.offspring.providers.bitcoinaverage.TickerAllProvider;
import com.dgex.offspring.providers.dgex.DGEXCurrentRateProvider;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrency;
import com.dgex.offspring.providers.service.IRate;
import com.dgex.offspring.providers.service.IRateProvider;

public class ExchangeRates {

  private static final Logger logger = Logger.getLogger(ExchangeRates.class);

  public static Double convertNxtToBtc(double value) {
    return value
        * getRate(DGEXCurrentRateProvider.getInstance(), Currencies.BTC,
            Currencies.NXT);
  }

  public static Double convertNxtToEur(double value) {
    return convertBtcToEur(convertNxtToBtc(value));
  }

  public static Double convertNxtToDollar(double value) {
    return convertBtcToDollar(convertNxtToBtc(value));
  }

  public static Double convertEurToBtc(double value) {
    return value
        / getRate(TickerAllProvider.getInstance(), Currencies.BTC,
            Currencies.EUR);
  }

  public static Double convertBtcToEur(double value) {
    return value
        * getRate(TickerAllProvider.getInstance(), Currencies.EUR,
            Currencies.BTC);
  }

  public static Double convertBtcToDollar(double value) {
    return value
        * getRate(TickerAllProvider.getInstance(), Currencies.USD,
            Currencies.BTC);
  }

  private static double getRate(IRateProvider provider, ICurrency base,
      ICurrency quote) {
    List<IRate> rates = provider.getRates(base, quote);

    // logger.info("Rates for " + base + "/" + quote + " " + rates);

    if (rates != null && !rates.isEmpty())
      return rates.get(0).getPrice();
    return 0;
  }
}
