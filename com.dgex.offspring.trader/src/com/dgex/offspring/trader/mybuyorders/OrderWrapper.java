package com.dgex.offspring.trader.mybuyorders;

import nxt.Order;

public class OrderWrapper {

  private final long price;
  private final int quantity;
  private final Long id;

  public OrderWrapper(Order.Bid order) {
    this.id = order.getId();
    this.price = order.getPrice();
    this.quantity = order.getQuantity();
  }

  public OrderWrapper(Long id, long price, int quantity) {
    this.id = id;
    this.price = price;
    this.quantity = quantity;
  }

  public Long getId() {
    return id;
  }

  public long getPrice() {
    return price;
  }

  public int getQuantity() {
    return quantity;
  }
}