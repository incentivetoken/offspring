package com.dgex.offspring.application.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.ProgressIndicator;

public class StartupProgressMonitor implements IProgressMonitor {

  private final UISynchronize sync;
  private final StartupDialog dialog;

  public StartupProgressMonitor(StartupDialog dialog, UISynchronize sync) {
    this.dialog = dialog;
    this.sync = sync;
  }

  @Override
  public void beginTask(final String name, final int totalWork) {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        ProgressIndicator p = dialog.getProgressIndicator();
        if (p != null && !p.isDisposed()) {
          p.beginAnimatedTask();
          dialog.setStatus(name);
        }
      }
    });
  }

  @Override
  public void worked(final int work) {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        ProgressIndicator p = dialog.getProgressIndicator();
        if (p != null && !p.isDisposed()) {
          p.worked(work);
        }
      }
    });
  }

  @Override
  public void done() {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        dialog.close();
      }
    });
  }

  @Override
  public void internalWorked(double work) {}

  @Override
  public boolean isCanceled() {
    return false;
  }

  @Override
  public void setCanceled(boolean value) {}

  @Override
  public void setTaskName(final String name) {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        dialog.setStatus(name);
      }
    });
  }

  @Override
  public void subTask(String name) {}

}
