package com.dgex.offspring.trader.api;

import nxt.Asset;

import org.eclipse.e4.core.services.events.IEventBroker;

public interface IAssetExchange {

  public static final String TOPIC_ASSET_SELECTED = "ASSETEXCHANGE/ASSET/SELECTED";

  public void initialize(IEventBroker broker);

  public void setSelectedAsset(Asset asset);

  public Asset getSelectedAsset();
}
