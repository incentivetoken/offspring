package com.dgex.offspring.application.ui.home;

import com.dgex.offspring.providers.service.ICurrencyPair;
import com.dgex.offspring.providers.service.ISellOrderProvider;

public class SellOrderTableInput {

  private final ISellOrderProvider provider;
  private final ICurrencyPair pair;

  public SellOrderTableInput(ISellOrderProvider provider, ICurrencyPair pair) {
    this.provider = provider;
    this.pair = pair;
  }

  public ISellOrderProvider getProvider() {
    return provider;
  }

  public ICurrencyPair getPair() {
    return pair;
  }

}
