package com.dgex.offspring.update;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.ProgressIndicator;

public class UpdateProgressMonitor implements IProgressMonitor {

  private final UISynchronize sync;
  private final UpdateDialog dialog;

  public UpdateProgressMonitor(UpdateDialog dialog, UISynchronize sync) {
    this.dialog = dialog;
    this.sync = sync;
  }

  @Override
  public void beginTask(final String name, final int totalWork) {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        dialog.showProgressIndicator();
        ProgressIndicator p = dialog.getProgressIndicator();
        if (p != null && !p.isDisposed()) {
          p.beginTask(totalWork);
          p.setToolTipText(name);
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
        ProgressIndicator p = dialog.getProgressIndicator();
        if (p != null && !p.isDisposed()) {
          p.sendRemainingWork();
        }
        dialog.hideProgressIndicator();
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
  public void setTaskName(String name) {}

  @Override
  public void subTask(String name) {}

}
