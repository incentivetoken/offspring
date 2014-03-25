package com.dgex.offspring.providers.service;

import java.util.Arrays;
import java.util.List;

import com.dgex.offspring.dataprovider.service.HTTPDataProvider;

public abstract class RateProvider extends HTTPDataProvider implements
    IRateProvider {

  @Override
  public List<IRate> getRates(ICurrency base, ICurrency quote) {
    ICurrencyPair pair = base.getPair(quote);
    if (Arrays.asList(getSupportedPairs()).indexOf(pair) == -1)
      throw new RuntimeException("Unsupported pair " + pair + " supported are "
          + Arrays.asList(getSupportedPairs()).toString());

    return doGetRates(pair);
  }

  @Override
  protected boolean doValidateSink(Object sink) {
    return sink instanceof IRateSink;
  }

  public abstract List<IRate> doGetRates(ICurrencyPair pair);

  @Override
  public String toString() {
    return "Rateprovider [" + getLabel() + "]";
  }
}
