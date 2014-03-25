package com.dgex.offspring.providers.service;

import java.util.Arrays;
import java.util.List;

import com.dgex.offspring.dataprovider.service.HTTPDataProvider;

public abstract class BuyOrderProvider extends HTTPDataProvider implements
    IBuyOrderProvider {

  @Override
  public List<IBuyOrder> getBuyOrders(ICurrency base, ICurrency quote) {
    ICurrencyPair pair = base.getPair(quote);
    if (Arrays.asList(getSupportedPairs()).indexOf(pair) == -1)
      throw new RuntimeException("Unsupported pair " + pair + " supported are "
          + Arrays.asList(getSupportedPairs()).toString());

    return doGetBuyOrders(pair);
  }

  public abstract List<IBuyOrder> doGetBuyOrders(ICurrencyPair pair);

  @Override
  protected boolean doValidateSink(Object sink) {
    return sink instanceof IBuyOrderSink;
  }

  @Override
  public String toString() {
    return "BuyOrderProvider [" + getLabel() + "]";
  }
}
