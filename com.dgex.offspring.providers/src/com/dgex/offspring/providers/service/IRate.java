package com.dgex.offspring.providers.service;

public interface IRate {

  public String getCurrencyPairKey();

  public ICurrency getBase();

  public ICurrency getQuote();

  /* Timestamp in milliseconds since epoch */
  public long getTimestamp();

  /* Avarage price */
  public double getPrice();

  /* Avarage ask price */
  public double getAskPrice();

  /* Avarage bid price */
  public double getBidPrice();

  /* Latest buy or sell price */
  public double getLastPrice();

  /* Volume */
  public double getTotalVol();
}
