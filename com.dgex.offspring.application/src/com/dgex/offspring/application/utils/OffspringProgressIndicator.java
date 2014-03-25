package com.dgex.offspring.application.utils;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.widgets.Composite;

public class OffspringProgressIndicator extends ProgressIndicator {

  static Logger logger = Logger.getLogger(OffspringProgressIndicator.class);

  private int interval = 100;

  private int total_work = 1000;

  public interface CompleteCallback {
    public boolean complete();
  };

  public OffspringProgressIndicator(Composite parent, int style) {
    super(parent, style);
  }

  /**
   * Runs a Thread that updates the progress bar.
   * 
   * @param duration
   * @param doneRunnable
   */
  public void beginWork(final int duration, final Runnable doneRunnable) {
    interval = 50;
    total_work = 1000;
    beginTask(total_work);
    new Thread(new Runnable() {
      @Override
      public void run() {
        final int iterations = duration / interval;
        for (final int[] i = new int[1]; i[0] <= iterations; i[0]++) {
          try {
            Thread.sleep(interval);
          }
          catch (Throwable th) {
          }
          getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
              worked(total_work / iterations);
            }
          });
        }
        getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            doneRunnable.run();
          }
        });
      }
    }).start();
  }

  public void finish() {
    logger.debug("finish");
    interval = 10;
  }
}
