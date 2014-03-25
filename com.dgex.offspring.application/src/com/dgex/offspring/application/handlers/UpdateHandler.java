package com.dgex.offspring.application.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.dataprovider.service.IDataProviderPool;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.update.UpdateDialog;

public class UpdateHandler {

  @CanExecute
  public boolean canExecute() {
    return false;
  }

  @Execute
  public void execute(Display display, Shell shell, UISynchronize sync,
      IProvisioningAgent agent, INxtService nxt, IDataProviderPool pool,
      IWorkbench workbench) {

    UpdateDialog dialog = new UpdateDialog(display, shell, sync, agent, pool,
        workbench);
    dialog.create();
    dialog.open();
  }
}