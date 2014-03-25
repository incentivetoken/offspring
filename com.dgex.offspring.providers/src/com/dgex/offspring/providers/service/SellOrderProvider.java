package com.dgex.offspring.providers.service;

import java.util.Arrays;
import java.util.List;

import com.dgex.offspring.dataprovider.service.HTTPDataProvider;

public abstract class SellOrderProvider extends HTTPDataProvider implements
    ISellOrderProvider {

  @Override
  public List<ISellOrder> getSellOrders(ICurrency base, ICurrency quote) {
    ICurrencyPair pair = base.getPair(quote);
    if (Arrays.asList(getSupportedPairs()).indexOf(pair) == -1)
      throw new RuntimeException("Unsupported pair " + pair + " supported are "
          + Arrays.asList(getSupportedPairs()).toString());

    return doGetSellOrders(pair);
  }

  public abstract List<ISellOrder> doGetSellOrders(ICurrencyPair pair);

  @Override
  protected boolean doValidateSink(Object sink) {
    return sink instanceof ISellOrderSink;
  }

  @Override
  public String toString() {
    return "SellOrderProvider [" + getLabel() + "]";
  }
}
