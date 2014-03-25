package com.dgex.offspring.providers.bitcoinaverage;

import org.apache.log4j.Logger;

import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrency;

public class PerHourMonthlyCNY extends PerHourMonthly {

  private static Logger logger = Logger.getLogger(PerHourMonthlyCNY.class);

  private static PerHourMonthlyCNY instance = null;

  public static PerHourMonthlyCNY getInstance() {

    logger.info("getInstance()");

    if (instance == null)
      instance = new PerHourMonthlyCNY(Currencies.CNY);
    return instance;
  }

  public PerHourMonthlyCNY(ICurrency base) {
    super(base);

    logger.info("new PerHourMonthlyCNY(" + base + "");
  }

}
