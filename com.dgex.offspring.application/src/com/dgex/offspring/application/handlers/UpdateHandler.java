package com.dgex.offspring.application.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;

import com.dgex.offspring.application.dialogs.UpdateCenterDialog;

public class UpdateHandler {

  @CanExecute
  public boolean canExecute() {
    return true;
  }

  @Execute
  public void execute(UISynchronize sync) {
    UpdateCenterDialog.show(sync);
  }
}