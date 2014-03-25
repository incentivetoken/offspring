package com.dgex.offspring.providers.service;

public class Trade implements ITrade {

  private final ICurrency base;
  private final ICurrency quote;
  private final double price;
  private final double quantity;
  private final String id;
  private final long timestamp;

  public Trade(ICurrency base, ICurrency quote, double price, double quantity,
      String id, long timestamp) {
    this.base = base;
    this.quote = quote;
    this.price = price;
    this.quantity = quantity;
    this.id = id;
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return "Trade " + base.getLabel() + "/" + quote.getLabel() + " " + price
        + " (" + quantity + ")";
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public double getPrice() {
    return price;
  }

  @Override
  public double getQuantity() {
    return quantity;
  }

  @Override
  public double getTotal() {
    return price * quantity;
  }

  @Override
  public ICurrency getBase() {
    return base;
  }

  @Override
  public ICurrency getQuote() {
    return quote;
  }

}
