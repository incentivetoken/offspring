package com.dgex.offspring.trader.api;

import nxt.Asset;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;

public class AssetExchangeImpl implements IAssetExchange {

  private IEventBroker broker = null;
  private Asset selectedAsset = null;

  @Override
  public void initialize(IEventBroker broker) {
    this.broker = broker;
  }

  @Override
  public void setSelectedAsset(final Asset asset) {
    if (asset != null && !asset.equals(selectedAsset)) {
      this.selectedAsset = asset;
      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

        @Override
        public void run() {
          if (broker != null)
            broker.send(IAssetExchange.TOPIC_ASSET_SELECTED, asset);
        }
      });
    }
  }

  @Override
  public Asset getSelectedAsset() {
    return selectedAsset;
  }
}
