package com.dgex.offspring.providers.dgex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.dgex.offspring.dataprovider.service.HTTPDataProviderException;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrencyPair;
import com.dgex.offspring.providers.service.IRate;
import com.dgex.offspring.providers.service.IRateSink;
import com.dgex.offspring.providers.service.Rate;
import com.dgex.offspring.providers.service.RateProvider;

public class DGEXCurrentRateProvider extends RateProvider {

  private static DGEXCurrentRateProvider instance = null;

  public static DGEXCurrentRateProvider getInstance() {
    if (instance == null)
      instance = new DGEXCurrentRateProvider();
    return instance;
  }

  private static Logger logger = Logger
      .getLogger(DGEXCurrentRateProvider.class);

  private static final String url = "https://dgex.com/API/nxtprice.txt";

  private final List<IRate> rates = new ArrayList<IRate>();

  private static final ICurrencyPair[] supportedPairs = { Currencies.BTC
      .getPair(Currencies.NXT) };

  @Override
  protected void doRun() {
    try {
      String data = get(new URL(url));
      double price = Double.parseDouble(data);
      long timestamp = 0l;
      rates.clear();
      IRate rate = new Rate(Currencies.BTC, Currencies.NXT, timestamp, price, 0);
      rates.add(rate);
      for (Object sink : sinks) {
        ((IRateSink) sink).addRate(rate);
      }
    }
    catch (MalformedURLException e) {
      logger.error("Mallformed URL " + url, e);
    }
    catch (HTTPDataProviderException e) {
      logger.error("HTTPDataProviderException " + url, e);
    }
    catch (IOException e) {
      logger.error("IOException " + url, e);
    }
  }

  @Override
  public String getLabel() {
    return "DGEX.com";
  }

  @Override
  public String toString() {
    return "DGEXCurrentRateProvider";
  }

  @Override
  public long getIntervalMilliseconds() {
    return 10 * 1000;
  }

  @Override
  public ICurrencyPair[] getSupportedPairs() {
    return supportedPairs;
  }

  @Override
  public List<IRate> doGetRates(ICurrencyPair pair) {
    return rates;
  }
}
