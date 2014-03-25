package com.dgex.offspring.providers.bitcoinaverage;

import org.apache.log4j.Logger;

import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrency;

public class PerHourMonthlyUSD extends PerHourMonthly {

  private static Logger logger = Logger.getLogger(PerHourMonthlyUSD.class);

  private static PerHourMonthlyUSD instance = null;

  public static PerHourMonthlyUSD getInstance() {

    logger.info("getInstance()");

    if (instance == null)
      instance = new PerHourMonthlyUSD(Currencies.USD);
    return instance;
  }

  public PerHourMonthlyUSD(ICurrency base) {
    super(base);

    logger.info("new PerHourMonthlyUSD(" + base + "");
  }
}
