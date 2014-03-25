package com.dgex.offspring.nxtCore.h2;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.h2.api.DatabaseEventListener;

/*
 * THIS IS DISABLED.
 * 
 * TO ENABLE ADD THIS CLASS TO H2 URL
 * 
 * ;DATABASE_EVENT_LISTENER='com.dgex.offspring.nxtCore.h2.H2Listener'
 */

public class H2Listener implements DatabaseEventListener {

  static Logger logger = Logger.getLogger(H2Listener.class);

  public H2Listener() {
    logger.warn("Creating com.dgex.offspring.nxtCore.h2.H2Listener");
  }

  @Override
  public void closingDatabase() {
    H2ListenerHelper.getInstance().getEventBroker()
        .post(IH2Listener.TOPIC_H2_CLOSING, 1);
  }

  @Override
  public void exceptionThrown(SQLException e, String sql) {
    logger.error("Exception Thrown " + e.getMessage() + " " + sql);
  }

  @Override
  public void init(String url) {
    H2ListenerHelper.getInstance().getEventBroker()
        .post(IH2Listener.TOPIC_H2_INIT, 1);
  }

  @Override
  public void opened() {
    H2ListenerHelper.getInstance().getEventBroker()
        .post(IH2Listener.TOPIC_H2_OPENED, 1);
  }

  @Override
  public void setProgress(int state, String name, int x, int max) {}
}
