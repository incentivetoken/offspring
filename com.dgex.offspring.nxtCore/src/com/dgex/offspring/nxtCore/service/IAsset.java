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

  public long getQuantityQNT();

  /**
   * Returns the number of assets owned by the issuer
   * 
   * @return
   */
  public long getAssetIssuerBalanceQNT();

  /**
   * Returns the unconfirmed number of assets owned by the issuer
   * 
   * @return
   */
  public long getAssetIssuerUnconfirmedBalanceQNT();

  public Account getIssuer();

  public String getDescription();

  public List<Order.Bid> getBidOrders();

  public List<Order.Ask> getAskOrders();

  public List<Trade> getTrades();
}
