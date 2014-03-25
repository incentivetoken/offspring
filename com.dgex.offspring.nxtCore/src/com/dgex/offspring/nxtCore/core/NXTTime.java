package com.dgex.offspring.nxtCore.core;

import nxt.Constants;

public class NXTTime {

  public static long convertTimestamp(long timestamp) {
    return ((timestamp * 1000) + Constants.EPOCH_BEGINNING - 500L);
  }
}
