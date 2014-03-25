package com.dgex.offspring.providers.bitcoinaverage;

import org.apache.log4j.Logger;

import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrency;

public class PerHourMonthlyEUR extends PerHourMonthly {

  private static Logger logger = Logger.getLogger(PerHourMonthlyEUR.class);

  private static PerHourMonthlyEUR instance = null;

  public static PerHourMonthlyEUR getInstance() {

    logger.info("getInstance()");

    if (instance == null)
      instance = new PerHourMonthlyEUR(Currencies.EUR);
    return instance;
  }

  public PerHourMonthlyEUR(ICurrency base) {
    super(base);

    logger.info("new PerHourMonthlyEUR(" + base + "");
  }
}
