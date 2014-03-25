package com.dgex.offspring.providers.service;

/**
 * Currencies are quoted in pairs, such as EUR/USD or USD/JPY. The first listed
 * currency is known as the base currency, while the second is called the
 * counter or quote currency. The base currency is the "basis" for the buy or
 * the sell. For example, if you BUY EUR/USD you have bought euros and
 * simultaneously sold dollars. You would do so in expectation that the euro
 * will appreciate (increase in value) relative to the US dollar.
 */
public interface ITrade {

  public ICurrency getBase();

  public ICurrency getQuote();

  public long getTimestamp();

  public String getId();

  public double getPrice();

  public double getQuantity();

  public double getTotal();
}
