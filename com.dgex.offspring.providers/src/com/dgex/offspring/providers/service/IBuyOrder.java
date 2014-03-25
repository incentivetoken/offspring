package com.dgex.offspring.providers.service;

/**
 * == IBuyOrder ==
 * 
 * On DGEX BTC/NXT exchange. If someone wants to buy NXT for a certain amount of
 * BTC. Then NXT is the base currency since some one wants to buy NXT, the quote
 * is BTC in this case.
 * 
 * On cryptsy exchange. If someone wants to buy Peercoin and pay in BTC. Then
 * Peercoin is the base and BTC the quote.
 * 
 * On MtGox exchange. If someone wants to buy BTC and pay with Dollars. Then BTC
 * is the base currency and Dollar the quote.
 * 
 * On distributed exchange. If someone wants to buy BTC-ASSET (places a bid
 * order) and pay in NXT (all assets are always paid in NXT). Then BTC-ASSET is
 * the base currency and NXT the quote.
 */

public interface IBuyOrder {

  /**
   * The base currency is the currency you are buying.
   */
  public ICurrency getBase();

  /**
   * The quote currency is the currency you will be paying with.
   */
  public ICurrency getQuote();

  public double getPrice();

  public double getQuantity();

  /**
   * Exchange specific order id
   */
  public String getOrderId();

  /**
   * Cumulative is calculated in the content provider
   */
  public void setCumulative(double value);

  public double getCumulative();
}
