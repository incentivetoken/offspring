package com.dgex.offspring.application.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.application.dialogs.UpdateCenterDialog;

public class UpdateHandler {

  @CanExecute
  public boolean canExecute() {
    return true;
  }

  @Execute
  public void execute(Display display, Shell shell, UISynchronize sync) {
    UpdateCenterDialog dialog = new UpdateCenterDialog(shell, sync);
    dialog.create();
    dialog.open();
  }
}