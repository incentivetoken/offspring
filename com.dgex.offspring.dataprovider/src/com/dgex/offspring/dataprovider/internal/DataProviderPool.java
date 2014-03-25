package com.dgex.offspring.dataprovider.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.dgex.offspring.dataprovider.service.IDataProvider;
import com.dgex.offspring.dataprovider.service.IDataProviderPool;

public class DataProviderPool implements IDataProviderPool {

  private static int scounter = 0;

  private static Logger logger = Logger.getLogger(DataProviderPool.class);

  private final List<IDataProvider> providers = new ArrayList<IDataProvider>();

  private final ScheduledExecutorService scheduledThreadPool = Executors
      .newScheduledThreadPool(2);

  private boolean destroyed = false;

  /* Runs a single DataProvider every second */
  private long intervalMilliseconds = 1000;

  public DataProviderPool() {
    scounter++;
    logger.info("new DataProviderPool #" + scounter);
    if (scounter > 1) {
      try {
        throw new Exception("Who ara you fool");
      }
      catch (Exception e) {
        logger.error("Show your self", e);
        System.exit(-1);
      }
    }
    scheduledThreadPool.scheduleWithFixedDelay(runnable, 100,
        intervalMilliseconds, TimeUnit.MILLISECONDS);
  }

  private final Runnable runnable = new Runnable() {

    @Override
    public void run() {
      IDataProvider provider = getNextProvider(providers);
      if (!destroyed && provider != null) {
        try {
          provider.run();
        }
        catch (Exception e) {
          logger.error("DataProviderPool exception. Provider: " + provider, e);
        }
      }
    }
  };

  @Override
  public void destroy() {
    destroyed = true;
    scheduledThreadPool.shutdown();
  }

  private static IDataProvider getNextProvider(List<IDataProvider> providers) {
    IDataProvider provider = null;
    long providerOverdue = 0l;
    for (IDataProvider p : providers) {
      long overdue = p.getTimeOverdue();
      if (overdue > providerOverdue) {
        provider = p;
        providerOverdue = overdue;
      }
    }
    return provider;
  }

  @Override
  public int getProviderCount() {
    return providers.size();
  }

  @Override
  public void addProvider(IDataProvider provider) {
    if (!providers.contains(provider))
      providers.add(provider);
  }

  @Override
  public void removeProvider(IDataProvider provider) {
    providers.remove(provider);
  }

  @Override
  public void setIntervalMilliseconds(long interval) {
    intervalMilliseconds = interval;
  }

  @Override
  public long getIntervalMilliseconds() {
    return intervalMilliseconds;
  }

  @Override
  public List<IDataProvider> getProviders() {
    return providers;
  }

}
