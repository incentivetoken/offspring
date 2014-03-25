package com.dgex.offspring.application.handlers;

import nxt.Nxt;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class DeleteBlocksHandler {

  @Execute
  public void execute(Shell shell) {
    if (MessageDialog
        .openConfirm(
            shell,
            "Reset Blockchain",
            "Are you sure you want to reset the blockchain?\nOffspring will re-download the blockchain for you.")) {
      Nxt.getBlockchainProcessor().fullReset();
    }
  }
}