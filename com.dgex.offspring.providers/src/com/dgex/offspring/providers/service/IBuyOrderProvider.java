package com.dgex.offspring.providers.service;

import java.util.List;

import com.dgex.offspring.dataprovider.service.IDataProvider;

public interface IBuyOrderProvider extends IDataProvider {

  public List<IBuyOrder> getBuyOrders(ICurrency base, ICurrency quote);

  public String getLabel();

  public ICurrencyPair[] getSupportedPairs();
}
