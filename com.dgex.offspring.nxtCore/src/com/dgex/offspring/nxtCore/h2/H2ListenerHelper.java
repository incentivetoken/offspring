package com.dgex.offspring.nxtCore.h2;

import org.eclipse.e4.core.services.events.IEventBroker;

public class H2ListenerHelper {

  private static H2ListenerHelper instance = null;

  private IEventBroker broker;

  public static H2ListenerHelper getInstance() {
    if (instance == null)
      instance = new H2ListenerHelper();
    return instance;
  }

  public void initialize(IEventBroker broker) {
    this.broker = broker;
  }

  public IEventBroker getEventBroker() {
    return broker;
  }
}
