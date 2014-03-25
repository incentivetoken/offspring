package com.dgex.offspring.providers.service;

public class SellOrder implements ISellOrder {

  private final ICurrency base;
  private final ICurrency quote;
  private final double price;
  private final double quantity;
  private final String orderId;
  private double cumulative = 0;

  public SellOrder(ICurrency base, ICurrency quote, double price,
      double quantity, String orderId) {
    this.base = base;
    this.quote = quote;
    this.price = price;
    this.quantity = quantity;
    this.orderId = orderId;
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
  public ICurrency getBase() {
    return base;
  }

  @Override
  public ICurrency getQuote() {
    return quote;
  }

  @Override
  public String getOrderId() {
    return orderId;
  }

  @Override
  public String toString() {
    return "SellOrder [" + base.getId() + "/" + quote.getId() + " " + price
        + " " + quantity + "]";
  }

  @Override
  public double getCumulative() {
    return cumulative;
  }

  @Override
  public void setCumulative(double value) {
    cumulative = value;
  }
}
