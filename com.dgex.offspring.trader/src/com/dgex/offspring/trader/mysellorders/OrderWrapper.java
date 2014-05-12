package com.dgex.offspring.trader.mysellorders;

import nxt.Asset;
import nxt.Order;

public class OrderWrapper {

  private final long priceNQT;
  private final long quantityQNT;
  private final Long id;
  private final Long assetId;
  private int decimals = -1;
  private String assetName = null;

  public OrderWrapper(Order.Ask order) {
    this.id = order.getId();
    this.assetId = order.getAssetId();
    this.priceNQT = order.getPriceNQT();
    this.quantityQNT = order.getQuantityQNT();
  }

  public OrderWrapper(Long id, long priceNQT, long quantityQNT, Long assetId) {
    this.id = id;
    this.priceNQT = priceNQT;
    this.quantityQNT = quantityQNT;
    this.assetId = assetId;
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

  public int getDecimals() {
    if (decimals == -1) {
      decimals = Asset.getAsset(assetId).getDecimals();
    }
    return decimals;
  }

  public String getAssetName() {
    if (assetName == null) {
      assetName = Asset.getAsset(assetId).getName();
    }
    return assetName;
  }
}