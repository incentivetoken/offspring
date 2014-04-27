package com.dgex.offspring.trader.mysellorders;

import nxt.Order;

public class OrderWrapper {

  private final long priceNQT;
  private final long quantityQNT;
  private final Long id;

  public OrderWrapper(Order.Ask order) {
    this.id = order.getId();
    this.priceNQT = order.getPriceNQT();
    this.quantityQNT = order.getQuantityQNT();
  }

  public OrderWrapper(Long id, long priceNQT, long quantityQNT) {
    this.id = id;
    this.priceNQT = priceNQT;
    this.quantityQNT = quantityQNT;
  }

  public Long getId() {
    return id;
  }

  public long getPriceNQT() {
    return priceNQT;
  }

  public long getQuantityQNT() {
    return quantityQNT;
  }
}