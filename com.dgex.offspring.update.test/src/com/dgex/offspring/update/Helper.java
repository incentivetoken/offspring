package com.dgex.offspring.update;

import org.eclipse.core.runtime.IProgressMonitor;

public class Helper {

  public static IProgressMonitor createProgressMonitor() {
    return new IProgressMonitor() {

      @Override
      public void beginTask(String name, int totalWork) {}

      @Override
      public void done() {}

      @Override
      public void internalWorked(double work) {}

      @Override
      public boolean isCanceled() {
        return false;
      }

      @Override
      public void setCanceled(boolean value) {}

      @Override
      public void setTaskName(String name) {}

      @Override
      public void subTask(String name) {}

      @Override
      public void worked(int work) {}
    };
  }

  public static UpdateLog createUpdateLog() {
    return new UpdateLog();
  }
}
