package com.dgex.offspring.application.ui.statusbar;

import nxt.Block;
import nxt.util.Convert;

import org.apache.log4j.Logger;

public class BlockchainDownloadMonitor {

  private static Logger logger = Logger
      .getLogger(BlockchainDownloadMonitor.class);

  private Block previousBlock = null;
  private ProgressIndicator progress = null;
  private long totalWork = 0;
  private long sumWorked = 0;
  private final long minimumPeriod = 1000 * 60 * 2; // 2 minutes

  public void setProgress(ProgressIndicator progress) {
    this.progress = progress;
    if (totalWork > sumWorked) {
      beginTask((int) totalWork);
      worked((int) sumWorked);
    }
    else {
      done();
    }
  }

  public void beginTask(int max) {
    totalWork = max;
    sumWorked = 0;
    if (progress != null && !progress.isDisposed())
      progress.beginTask(max);
  }

  public void worked(int worked) {
    sumWorked += worked;
    if (progress != null && !progress.isDisposed())
      progress.worked(worked);
  }

  public void done() {
    totalWork = sumWorked = 0;
    if (progress != null && !progress.isDisposed())
      progress.done();
  }

  public boolean isActive() {
    return totalWork != sumWorked;
  }

  public long remainingMilliseconds() {
    return totalWork - sumWorked;
  }

  public void blockPushed(Block block) {
    if (isActive()) {
      long worked = calculateWorked(block);
      worked((int) worked);
      if ((totalWork - sumWorked) < minimumPeriod) {
        done();
      }
      else {
        previousBlock = block;
      }
    }
    else {
      sumWorked = 0;
      totalWork = Convert.getEpochTime() - block.getTimestamp();
      if (totalWork > minimumPeriod) {
        beginTask((int) totalWork);
        previousBlock = block;
      }
      else {
        totalWork = sumWorked = 0;
      }
    }
  }

  private long calculateWorked(Block block) {
    long previous = previousBlock == null ? (block.getTimestamp() - 1)
        : previousBlock.getTimestamp();
    return block.getTimestamp() - previous;
  }
}
