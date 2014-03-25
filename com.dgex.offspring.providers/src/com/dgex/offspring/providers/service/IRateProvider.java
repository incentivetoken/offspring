package com.dgex.offspring.providers.service;

import java.util.List;

import com.dgex.offspring.dataprovider.service.IDataProvider;

public interface IRateProvider extends IDataProvider {

  public List<IRate> getRates(ICurrency base, ICurrency quote);

  public String getLabel();

  public ICurrencyPair[] getSupportedPairs();
}
