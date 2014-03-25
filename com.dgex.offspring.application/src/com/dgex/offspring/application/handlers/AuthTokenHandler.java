package com.dgex.offspring.application.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.application.dialogs.AuthTokenDialog;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.user.service.IUserService;

public class AuthTokenHandler {

  @Execute
  public void execute(Shell shell, IUserService userService, INxtService nxt,
      UISynchronize sync) {
    AuthTokenDialog dialog = new AuthTokenDialog(shell, nxt, userService);
    dialog.create();
    dialog.setBlockOnOpen(true);
    if (dialog.open() == Window.OK) {

    }
  }

}