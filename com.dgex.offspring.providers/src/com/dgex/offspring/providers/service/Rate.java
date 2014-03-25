package com.dgex.offspring.providers.service;

public class Rate implements IRate {

  private final ICurrency base;
  private final ICurrency quote;
  private final long timestamp;
  private final double price;
  private final double totalVol;
  private final double askPrice;
  private final double bidPrice;
  private final double lastPrice;
  private final String currencyPair;

  public Rate(ICurrencyPair pair, long timestamp, double price, double totalVol) {
    this(pair.getBase(), pair.getQuote(), timestamp, price, totalVol, 0, 0, 0);
  }

  public Rate(ICurrency base, ICurrency quote, long timestamp, double price,
      double totalVol) {
    this(base, quote, timestamp, price, totalVol, 0, 0, 0);
  }

  public Rate(ICurrency base, ICurrency quote, long timestamp, double price,
      double totalVol, double askPrice, double bidPrice, double lastPrice) {
    this.base = base;
    this.quote = quote;
    this.timestamp = timestamp;
    this.price = price;
    this.totalVol = totalVol;
    this.askPrice = 0;
    this.bidPrice = 0;
    this.lastPrice = 0;
    this.currencyPair = base.getId() + quote.getId();
  }

  @Override
  public String toString() {
    return "Rate " + currencyPair + " price=" + price + " timestamp="
        + timestamp;
  }

  @Override
  public ICurrency getBase() {
    return base;
  }

  @Override
  public ICurrency getQuote() {
    return quote;
  }

  @Override
  public double getPrice() {
    return price;
  }

  @Override
  public String getCurrencyPairKey() {
    return currencyPair;
  }

  @Override
  public double getAskPrice() {
    return askPrice;
  }

  @Override
  public double getBidPrice() {
    return bidPrice;
  }

  @Override
  public double getLastPrice() {
    return lastPrice;
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public double getTotalVol() {
    return totalVol;
  }

}
