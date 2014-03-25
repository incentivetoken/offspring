package com.dgex.offspring.trader.api;

import nxt.Block;
import nxt.Nxt;
import nxt.Trade;

public class ExchangeUtils {

  public static int getTimestamp(Trade trade) {
    Block block = Nxt.getBlockchain().getBlock(trade.getBlockId());
    if (block != null) {
      return block.getTimestamp();
    }
    return 0;
  }
}
