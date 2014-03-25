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
import com.dgex.offspring.providers.service.BuyOrder;
import com.dgex.offspring.providers.service.BuyOrderProvider;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.IBuyOrder;
import com.dgex.offspring.providers.service.IBuyOrderSink;
import com.dgex.offspring.providers.service.ICurrencyPair;
import com.dgex.offspring.providers.service.JSONStructureException;
import com.dgex.offspring.providers.utils.JSONUtils;

public class DGEXBuyOrderProvider extends BuyOrderProvider {

  private static DGEXBuyOrderProvider instance = null;

  public static DGEXBuyOrderProvider getInstance() {
    if (instance == null)
      instance = new DGEXBuyOrderProvider();
    return instance;
  }

  private static Logger logger = Logger.getLogger(DGEXBuyOrderProvider.class);

  private static final String url = "https://dgex.com/API/buy.json";

  private static final String BID = "bid";
  private static final String ORDERNUMBER = "ordernumber";
  private static final String TIMESTAMP = "timestamp";
  private static final String UNITPRICE = "unitprice";
  private static final String UNITS = "units";

  private final List<IBuyOrder> orders = new ArrayList<IBuyOrder>();

  private static final ICurrencyPair[] supportedPairs = { Currencies.NXT
      .getPair(Currencies.BTC) };

  @Override
  public long getIntervalMilliseconds() {
    return 30 * 1000; // 30 seconds
  }

  @Override
  public String getLabel() {
    return "DGEX Buy Orders";
  }

  @Override
  public ICurrencyPair[] getSupportedPairs() {
    return supportedPairs;
  }

  @Override
  public List<IBuyOrder> doGetBuyOrders(ICurrencyPair pair) {
    return orders;
  }

  @Override
  protected void doRun() {
    try {
      String data = get(new URL(url));

      Object x = new JSONParser().parse(data);
      if (!(x instanceof JSONObject))
        throw new JSONStructureException();

      JSONArray ask = JSONUtils.getList((JSONObject) x, BID);
      if (ask == null)
        throw new JSONStructureException();

      orders.clear();
      for (Object o : ask) {
        if (!(o instanceof JSONObject))
          throw new JSONStructureException();

        JSONObject map = (JSONObject) o;
        double price = JSONUtils.getDouble(map, UNITPRICE);
        double quantity = JSONUtils.getDouble(map, UNITS);
        long timestamp = JSONUtils.getLong(map, TIMESTAMP) * 1000;
        String id = JSONUtils.getString(map, ORDERNUMBER);

        IBuyOrder order = new BuyOrder(Currencies.NXT, Currencies.BTC, price,
            quantity, id);
        orders.add(order);

        // logger.info(" >> " + order);

        for (Object sink : sinks) {
          ((IBuyOrderSink) sink).addBuyOrder(order);
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
    catch (JSONStructureException e) {
      logger.error("JSONStructureException " + url, e);
    }
  }
}
