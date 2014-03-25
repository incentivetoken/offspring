package com.dgex.offspring.application.ui.home;

import com.dgex.offspring.providers.service.IBuyOrderProvider;
import com.dgex.offspring.providers.service.ICurrencyPair;

public class BuyOrderTableInput {

  private final IBuyOrderProvider provider;
  private final ICurrencyPair pair;

  public BuyOrderTableInput(IBuyOrderProvider provider, ICurrencyPair pair) {
    this.provider = provider;
    this.pair = pair;
  }

  public IBuyOrderProvider getProvider() {
    return provider;
  }

  public ICurrencyPair getPair() {
    return pair;
  }
}
