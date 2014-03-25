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

import com.dgex.offspring.dataprovider.service.HTTPDataProvider;
import com.dgex.offspring.dataprovider.service.HTTPDataProviderException;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.IBuyOrder;
import com.dgex.offspring.providers.service.IExchange;
import com.dgex.offspring.providers.service.ISellOrder;
import com.dgex.offspring.providers.service.ITrade;
import com.dgex.offspring.providers.service.ITradeSink;
import com.dgex.offspring.providers.service.Trade;
import com.dgex.offspring.providers.utils.JSONUtils;

public class DGEXTradeProvider extends HTTPDataProvider implements IExchange {

  private static DGEXTradeProvider instance = null;

  public static DGEXTradeProvider getInstance() {
    if (instance == null)
      instance = new DGEXTradeProvider();
    return instance;
  }

  private static Logger logger = Logger.getLogger(DGEXTradeProvider.class);

  private static final String url = "https://dgex.com/API/trades3h.json";

  private static final String TICKER = "ticker";

  private final static String TIME = "time";

  private final static String TIMESTAMP = "timestamp";

  private final static String UNITS = "units";

  private final static String UNITPRICE = "unitprice";

  private final List<ITrade> trades = new ArrayList<ITrade>();

  @Override
  public long getIntervalMilliseconds() {
    return 10 * 1000;
  }

  @Override
  protected void doRun() {
    try {
      String data = get(new URL(url));
      Object x = new JSONParser().parse(data);
      if (x instanceof JSONObject) {
        JSONArray ticker = JSONUtils.getList((JSONObject) x, TICKER);
        if (ticker != null) {
          trades.clear();
          for (Object o : ticker) {
            if (o instanceof JSONObject) {
              JSONObject map = (JSONObject) o;
              ITrade trade = new Trade(Currencies.BTC, Currencies.NXT,
                  JSONUtils.getDouble(map, UNITPRICE), JSONUtils.getDouble(map,
                      UNITS), "id", 0l);
              trades.add(trade);
              for (Object sink : sinks) {
                ((ITradeSink) sink).addTrade(trade);
              }
            }
          }

          // logger.info("Trades:\n\n" + trades);

        }
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
    catch (ParseException e) {
      logger.error("ParseException " + url, e);
    }
  }

  @Override
  protected boolean doValidateSink(Object sink) {
    return sink instanceof ITradeSink;
  }

  @Override
  public List<ITrade> getTrades() {
    return trades;
  }

  @Override
  public List<IBuyOrder> getBuyOrders() {
    return null;
  }

  @Override
  public List<ISellOrder> getSellOrders() {
    return null;
  }

}
