package com.dgex.offspring.providers.bitcoinaverage;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

import com.dgex.offspring.dataprovider.service.HTTPDataProviderException;
import com.dgex.offspring.providers.service.CSVStructureException;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrency;
import com.dgex.offspring.providers.service.ICurrencyPair;
import com.dgex.offspring.providers.service.IRate;
import com.dgex.offspring.providers.service.IRateSink;
import com.dgex.offspring.providers.service.Rate;
import com.dgex.offspring.providers.service.RateProvider;

public class PerMinute24HSliding extends RateProvider {

  private static PerMinute24HSliding instance = null;

  public static PerMinute24HSliding getInstance() {
    if (instance == null)
      instance = new PerMinute24HSliding();
    return instance;
  }

  private static Logger logger = Logger.getLogger(PerMinute24HSliding.class);

  /* We can always use the USD version */
  private static final String url = "https://api.bitcoinaverage.com/history/USD/per_minute_24h_global_average_sliding_window.csv";

  private final Map<ICurrencyPair, List<IRate>> rates = new HashMap<ICurrencyPair, List<IRate>>();

  // private static final String[] headers = { "datetime", "USD volume",
  // "USD average", "USD rate", "EUR volume", "EUR average", "EUR rate",
  // "CNY volume", "CNY average", "CNY rate", "GBP volume", "GBP average",
  // "GBP rate", "CAD volume", "CAD average", "CAD rate", "PLN volume",
  // "PLN average", "PLN rate", "JPY volume", "JPY average", "JPY rate",
  // "RUB volume", "RUB average", "RUB rate", "AUD volume", "AUD average",
  // "AUD rate", "SEK volume", "SEK average", "SEK rate", "BRL volume",
  // "BRL average", "BRL rate", "NZD volume", "NZD average", "NZD rate",
  // "SGD volume", "SGD average", "SGD rate", "ZAR volume", "ZAR average",
  // "ZAR rate", "NOK volume", "NOK average", "NOK rate", "ILS volume",
  // "ILS average", "ILS rate", "CHF volume", "CHF average", "CHF rate",
  // "TRY volume", "TRY average", "TRY rate", "USD global average" };

  private static final String[] headers = { "datetime", "USD volume",
      "USD average", "USD rate", "EUR volume", "EUR average", "EUR rate",
      "CNY volume", "CNY average", "CNY rate", "GBP volume", "GBP average",
      "GBP rate", "CAD volume", "CAD average", "CAD rate", "PLN volume",
      "PLN average", "PLN rate", "RUB volume", "RUB average", "RUB rate",
      "AUD volume", "AUD average", "AUD rate", "SEK volume", "SEK average",
      "SEK rate", "BRL volume", "BRL average", "BRL rate", "NZD volume",
      "NZD average", "NZD rate", "SGD volume", "SGD average", "SGD rate",
      "ZAR volume", "ZAR average", "ZAR rate", "NOK volume", "NOK average",
      "NOK rate", "ILS volume", "ILS average", "ILS rate", "CHF volume",
      "CHF average", "CHF rate", "TRY volume", "TRY average", "TRY rate",
      "HKD volume", "HKD average", "HKD rate", "RON volume", "RON average",
      "RON rate", "MXN volume", "MXN average", "MXN rate", "USD global average" };

  private final int DATETIME_INDEX = Arrays.asList(headers).indexOf("datetime");

  private final int USD_VOLUME = Arrays.asList(headers).indexOf("USD volume");
  private final int USD_AVERAGE = Arrays.asList(headers).indexOf(
      "USD global average");

  private final int EUR_VOLUME = Arrays.asList(headers).indexOf("EUR volume");
  private final int EUR_AVERAGE = Arrays.asList(headers).indexOf("EUR average");

  private final int CNY_VOLUME = Arrays.asList(headers).indexOf("CNY volume");
  private final int CNY_AVERAGE = Arrays.asList(headers).indexOf("CNY average");

  private final int GBP_VOLUME = Arrays.asList(headers).indexOf("GBP volume");
  private final int GBP_AVERAGE = Arrays.asList(headers).indexOf("GBP volume");

  private final int JPY_VOLUME = Arrays.asList(headers).indexOf("JPY volume");
  private final int JPY_AVERAGE = Arrays.asList(headers).indexOf("JPY volume");

  /* Add currencies and their indexes to have the run method extract those pairs */
  private final Object[][] smart = {
      { Currencies.USD, USD_VOLUME, USD_AVERAGE },
      { Currencies.EUR, EUR_VOLUME, EUR_AVERAGE },
      { Currencies.CNY, CNY_VOLUME, CNY_AVERAGE },
      { Currencies.GBP, GBP_VOLUME, GBP_AVERAGE },
      { Currencies.JPY, JPY_VOLUME, JPY_AVERAGE }, };

  private static final ICurrencyPair[] supportedPairs = {
      Currencies.USD.getPair(Currencies.BTC),
      Currencies.EUR.getPair(Currencies.BTC),
      Currencies.CNY.getPair(Currencies.BTC),
      Currencies.GBP.getPair(Currencies.BTC),
      Currencies.JPY.getPair(Currencies.BTC), };

  @Override
  public ICurrencyPair[] getSupportedPairs() {
    return supportedPairs;
  }

  @Override
  public List<IRate> doGetRates(ICurrencyPair pair) {
    return rates.get(pair);
  }

  // 2014-02-09 14:38:44
  private static long parseDate(String dateString) {
    try {
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
          Locale.ENGLISH);
      return df.parse(dateString).getTime();
    }
    catch (java.text.ParseException e) {
      logger.error("Date=" + dateString, e);
    }
    return 0l;
  }

  @Override
  public long getIntervalMilliseconds() {
    return 60 * 1000 * 5; // every 5 minutes
  }

  @Override
  public String getLabel() {
    return "Bitcoinaverage.com per minute 24H sliding";
  }

  @Override
  protected void doRun() {
    try {
      String data = get(new URL(url));

      List<String[]> entries = new ArrayList<String[]>();
      StringReader strReader = new StringReader(data);
      CSVReader csvReader = new CSVReader(strReader);
      try {
        entries = csvReader.readAll();
      }
      finally {
        strReader.close();
        csvReader.close();
      }

      if (entries == null || entries.size() == 0)
        throw new CSVStructureException();

      /* Ensure that the headers match (1) */
      if (headers.length != entries.get(0).length) {
        logger.error("Expected length " + headers.length + " got "
            + entries.get(0).length);
        throw new CSVStructureException();
      }

      /* Ensure that the headers match (2) */
      for (int i = 0; i < headers.length; i++) {
        if (!headers[i].equals(entries.get(0)[i]))
          throw new CSVStructureException();
      }

      entries.remove(0);
      rates.clear();

      for (String[] fields : entries) {
        long timestamp = parseDate(fields[DATETIME_INDEX]);
        for (int i = 0; i < smart.length; i++) {
          ICurrency currency = (ICurrency) smart[i][0];
          int index_volume = (Integer) smart[i][1];
          int index_average = (Integer) smart[i][2];

          double volume = Double.parseDouble(fields[index_volume]);
          double average = Double.parseDouble(fields[index_average]);

          IRate rate = new Rate(currency, Currencies.BTC, timestamp, average,
              volume);

          ICurrencyPair pair = currency.getPair(Currencies.BTC);
          List<IRate> list = rates.get(pair);
          if (list == null) {
            list = new ArrayList<IRate>();
            rates.put(pair, list);
          }
          list.add(rate);

          for (Object sink : sinks) {
            ((IRateSink) sink).addRate(rate);
          }
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
    catch (CSVStructureException e) {
      logger.error("IOException " + url, e);
    }
  }
}
