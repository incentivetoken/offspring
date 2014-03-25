package com.dgex.offspring.providers.dgex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.dgex.offspring.dataprovider.service.HTTPDataProviderException;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrencyPair;
import com.dgex.offspring.providers.service.IRate;
import com.dgex.offspring.providers.service.IRateSink;
import com.dgex.offspring.providers.service.JSONStructureException;
import com.dgex.offspring.providers.service.Rate;
import com.dgex.offspring.providers.service.RateProvider;
import com.dgex.offspring.providers.utils.JSONUtils;

public class DGEX3HMovingAvarage extends RateProvider {

  private static DGEX3HMovingAvarage instance = null;

  public static DGEX3HMovingAvarage getInstance() {
    if (instance == null)
      instance = new DGEX3HMovingAvarage();
    return instance;
  }

  private static Logger logger = Logger.getLogger(DGEX3HMovingAvarage.class);

  private static final String url = "https://dgex.com/API/3hma.json";

  private static final String AVARAGES = "averages";

  // private final static String TIME = "time";

  private final static String TIMESTAMP = "timestamp";

  private final static String UNITS = "units";

  private final static String UNITPRICE = "unitprice";

  private final List<IRate> rates = new ArrayList<IRate>();

  private static final ICurrencyPair[] supportedPairs = { Currencies.BTC
      .getPair(Currencies.NXT) };

  @Override
  public String getLabel() {
    return "DGEX.com 3H moving average";
  }

  @Override
  public long getIntervalMilliseconds() {
    return 10 * 1000;
  }

  @Override
  protected void doRun() {
    try {
      String data = get(new URL(url));

      Object x = new JSONParser().parse(data);
      if (!(x instanceof JSONObject))
        throw new JSONStructureException();

      JSONArray ticker = JSONUtils.getList((JSONObject) x, AVARAGES);
      if (ticker == null)
        throw new JSONStructureException();

      rates.clear();
      for (Object o : ticker) {
        if (!(o instanceof JSONObject))
          throw new JSONStructureException();

        JSONObject map = (JSONObject) o;
        double price = JSONUtils.getDouble(map, UNITPRICE);
        double vol = JSONUtils.getDouble(map, UNITS);
        long timestamp = JSONUtils.getLong(map, TIMESTAMP) * 1000;

        IRate rate = new Rate(Currencies.BTC, Currencies.NXT, timestamp, price,
            vol);
        rates.add(rate);
        for (Object sink : sinks) {
          ((IRateSink) sink).addRate(rate);
        }
      }
      notifyListeners();
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
    catch (ParseException e) {
      logger.error("ParseException " + url, e);
    }
    catch (JSONStructureException e) {
      logger.error("JSONStructureException " + url, e);
    }
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
