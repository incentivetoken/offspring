package com.dgex.offspring.dataprovider.internal;

import java.util.ArrayList;
import java.util.List;

import com.dgex.offspring.dataprovider.service.IDataProvider;
import com.dgex.offspring.dataprovider.service.IDataProviderListener;

public abstract class DataProvider implements IDataProvider {

  protected final List<Object> sinks = new ArrayList<Object>();

  protected final List<IDataProviderListener> listeners = new ArrayList<IDataProviderListener>();

  /* Default interval is 5 seconds */
  private final long defaultInterval = 10000;

  private long lastRunCurrentTime = 0l;

  @Override
  public void addSink(Object sink) {
    if (!doValidateSink(sink))
      throw new RuntimeException("Illegal Sink Type");

    if (!sinks.contains(sink))
      sinks.add(sink);
  }

  @Override
  public void removeSink(Object sink) {
    sinks.remove(sink);
  }

  @Override
  public void run() {
    lastRunCurrentTime = System.currentTimeMillis();
    doRun();
    notifyListeners();
  }

  @Override
  public long getIntervalMilliseconds() {
    return defaultInterval;
  }

  @Override
  public long getTimePassedSinceLastRun() {
    return System.currentTimeMillis() - lastRunCurrentTime;
  }

  @Override
  public long getTimeOverdue() {
    return getTimePassedSinceLastRun() - getIntervalMilliseconds();
  }

  @Override
  public void addDataProviderListener(IDataProviderListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeDataProviderListener(IDataProviderListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void bump() {
    lastRunCurrentTime = System.currentTimeMillis() - defaultInterval;
  }

  protected void notifyListeners() {
    for (IDataProviderListener listener : listeners) {
      listener.update();
    }
  }

  protected abstract void doRun();

  protected abstract boolean doValidateSink(Object sink);
}
