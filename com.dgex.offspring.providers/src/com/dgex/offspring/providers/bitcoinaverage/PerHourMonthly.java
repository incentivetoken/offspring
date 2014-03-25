package com.dgex.offspring.providers.bitcoinaverage;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.dgex.offspring.dataprovider.service.HTTPDataProviderException;
import com.dgex.offspring.providers.service.CSVStructureException;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrency;
import com.dgex.offspring.providers.service.ICurrencyPair;
import com.dgex.offspring.providers.service.IRate;
import com.dgex.offspring.providers.service.IRateSink;
import com.dgex.offspring.providers.service.Rate;
import com.dgex.offspring.providers.service.RateProvider;

import au.com.bytecode.opencsv.CSVReader;

public class PerHourMonthly extends RateProvider {

  private static Logger logger = Logger.getLogger(PerHourMonthly.class);

  private final List<IRate> rates = new ArrayList<IRate>();

  protected static final String url = "https://api.bitcoinaverage.com/history/CURRENCY/per_hour_monthly_sliding_window.csv";

  private static final String[] headers = { "datetime", "high", "low",
      "average" };

  private static final int DATETIME_INDEX = 0; // datetime
  private static final int HIGH_INDEX = 1;     // high
  private static final int LOW_INDEX = 2;      // low
  private static final int AVERAGE_INDEX = 3;  // avarage

  private final ICurrency base;

  public PerHourMonthly(ICurrency base) {
    this.base = base;

    logger.info("new PerHourMonthly(" + base + "");

  }

  @Override
  public String getLabel() {
    return "Bitcoinaverage.com BTC/EUR per hour monthly";
  }

  // 2014-01-10 16:34:09
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
  protected void doRun() {
    try {
      String data = get(new URL(url.replaceAll("CURRENCY", base.getId())));
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

      /* Ensure that the headers match */
      if (headers.length != entries.get(0).length) {
        logger.error("Expected length " + headers.length + " got "
            + entries.get(0).length);
        throw new CSVStructureException();
      }

      for (int i = 0; i < headers.length; i++) {
        if (!headers[i].equals(entries.get(0)[i]))
          throw new CSVStructureException();
      }

      entries.remove(0);
      rates.clear();

      for (String[] fields : entries) {
        long timestamp = parseDate(fields[DATETIME_INDEX]);
        double avarage = Double.parseDouble(fields[AVERAGE_INDEX]);
        IRate rate = new Rate(base, Currencies.BTC, timestamp, avarage, 0);
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
    catch (CSVStructureException e) {
      logger.error("IOException " + url, e);
    }
  }

  @Override
  public ICurrencyPair[] getSupportedPairs() {
    return new ICurrencyPair[] { base.getPair(Currencies.BTC) };
  }

  @Override
  public List<IRate> doGetRates(ICurrencyPair pair) {
    return rates;
  }
}
