package com.dgex.offspring.nxtCore.service;

import java.util.List;

import nxt.Account;
import nxt.Asset;
import nxt.Order;
import nxt.Trade;

public interface IAsset {

  public Asset getNative();

  public String getName();

  public Long getId();

  public int getQuantity();

  /**
   * Returns the number of assets owned by the issuer
   * 
   * @return
   */
  public int getAssetIssuerBalance();

  /**
   * Returns the unconfirmed number of assets owned by the issuer
   * 
   * @return
   */
  public int getAssetIssuerUnconfirmedBalance();

  public Account getIssuer();

  public String getDescription();

  public List<Order.Bid> getBidOrders();

  public List<Order.Ask> getAskOrders();

  public List<Trade> getTrades();
}
