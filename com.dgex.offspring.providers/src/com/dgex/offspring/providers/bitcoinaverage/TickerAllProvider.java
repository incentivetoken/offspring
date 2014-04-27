package com.dgex.offspring.providers.bitcoinaverage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.dgex.offspring.dataprovider.service.HTTPDataProviderException;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrency;
import com.dgex.offspring.providers.service.ICurrencyPair;
import com.dgex.offspring.providers.service.IRate;
import com.dgex.offspring.providers.service.IRateSink;
import com.dgex.offspring.providers.service.Rate;
import com.dgex.offspring.providers.service.RateProvider;
import com.dgex.offspring.providers.utils.JSONUtils;

public class TickerAllProvider extends RateProvider {

  private static TickerAllProvider instance = null;

  public static TickerAllProvider getInstance() {
    if (instance == null)
      instance = new TickerAllProvider();
    return instance;
  }

  private static Logger logger = Logger.getLogger(TickerAllProvider.class);

  private static final String url = "https://api.bitcoinaverage.com/ticker/all";

  private static final String _24HOUR_AVERAGE = "24h_avg";
  private static final String ASK = "ask";
  private static final String BID = "bid";
  private static final String LAST = "last";
  private static final String TIMESTAMP = "timestamp";
  private static final String TOTAL_VOL = "total_vol";

  private final Map<ICurrencyPair, List<IRate>> rates = new HashMap<ICurrencyPair, List<IRate>>();

  private static final ICurrencyPair[] supportedPairs = {
      // Currencies.AUD.getPair(Currencies.BTC),
      // Currencies.BRL.getPair(Currencies.BTC),
      // Currencies.CAD.getPair(Currencies.BTC),
      // Currencies.CHF.getPair(Currencies.BTC),
      Currencies.CNY.getPair(Currencies.BTC),
      Currencies.EUR.getPair(Currencies.BTC),
      // Currencies.GBP.getPair(Currencies.BTC),
      // Currencies.ILS.getPair(Currencies.BTC),
      // Currencies.JPY.getPair(Currencies.BTC),
      // Currencies.NOK.getPair(Currencies.BTC),
      // Currencies.NZD.getPair(Currencies.BTC),
      // Currencies.PLN.getPair(Currencies.BTC),
      // Currencies.RUB.getPair(Currencies.BTC),
      // Currencies.SEK.getPair(Currencies.BTC),
      // Currencies.SGD.getPair(Currencies.BTC),
      // Currencies.TRY.getPair(Currencies.BTC),
      Currencies.USD.getPair(Currencies.BTC),
  // Currencies.ZAR.getPair(Currencies.BTC)
  };

  @Override
  public String getLabel() {
    return "Bitcoinaverage.com";
  }

  @Override
  public long getIntervalMilliseconds() {
    return 10 * 1000;
  }

  // Sun, 09 Feb 2014 14:17:51 -0000
  private static long parseDate(String dateString) {
    try {
      DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",
          Locale.ENGLISH);
      return df.parse(dateString).getTime();
    }
    catch (java.text.ParseException e) {
      logger.trace("Date=" + dateString, e);
    }
    return 0l;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void doRun() {
    try {
      String data = get(new URL(url));

      Object x = new JSONParser().parse(data);
      if (!(x instanceof JSONObject)) {
        logger.error("Expected JSONObject");
        return;
      }

      Map<String, Object> map = (Map<String, Object>) x;
      for (String c : map.keySet()) {
        if ("timestamp".equals(c))
          continue;

        ICurrency currency = Currencies.getCurrency(c);
        if (currency == null) {
          logger.error("Unsupported currency: " + c);
          continue;
        }

        Object obj = map.get(c);
        if (!(obj instanceof JSONObject)) {
          logger.error("Expecting JSONObject: " + obj);
          continue;
        }

        JSONObject mapObj = (JSONObject) obj;
        double price = JSONUtils.getDouble(mapObj, _24HOUR_AVERAGE);
        double last = JSONUtils.getDouble(mapObj, LAST);
        double ask = JSONUtils.getDouble(mapObj, ASK);
        double bid = JSONUtils.getDouble(mapObj, BID);
        double vol = JSONUtils.getDouble(mapObj, TOTAL_VOL);
        long timestamp = parseDate(JSONUtils.getString(mapObj, TIMESTAMP));

        // logger.info(" > timestamp=" + timestamp + " date="
        // + new Date(timestamp));

        IRate rate = new Rate(currency, Currencies.BTC, timestamp, price, vol,
            last, ask, bid);

        ICurrencyPair pair = currency.getPair(Currencies.BTC);

        List<IRate> temp = new ArrayList<IRate>();
        temp.add(rate);
        rates.put(pair, temp);

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
  }

  @Override
  public ICurrencyPair[] getSupportedPairs() {
    return supportedPairs;
  }

  @Override
  public List<IRate> doGetRates(ICurrencyPair pair) {
    return rates.get(pair);
  }

}
