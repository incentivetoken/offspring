package com.dgex.offspring.providers.service;

import java.util.List;

import com.dgex.offspring.dataprovider.service.IDataProvider;

public interface ISellOrderProvider extends IDataProvider {

  public List<ISellOrder> getSellOrders(ICurrency base, ICurrency quote);

  public String getLabel();

  public ICurrencyPair[] getSupportedPairs();
}
