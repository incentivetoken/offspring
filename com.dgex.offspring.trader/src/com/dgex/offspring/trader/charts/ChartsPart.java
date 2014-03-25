package com.dgex.offspring.trader.charts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.Asset;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.trader.api.IAssetExchange;

public class ChartsPart {

  private AEChartComposite chartsComposite;

  @PostConstruct
  public void postConstruct(Composite parent) {
    chartsComposite = new AEChartComposite(parent, SWT.NONE);
  }

  @Inject
  @Optional
  private void onAssetSelected(
      @UIEventTopic(IAssetExchange.TOPIC_ASSET_SELECTED) Asset asset) {
    if (chartsComposite != null && !chartsComposite.isDisposed()) {
      chartsComposite.refresh(asset);
    }
  }

}