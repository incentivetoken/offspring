package com.dgex.offspring.providers.service;

import java.util.List;

public interface IExchange {

  public List<ITrade> getTrades();

  public List<IBuyOrder> getBuyOrders();

  public List<ISellOrder> getSellOrders();

}
