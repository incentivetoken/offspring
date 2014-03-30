 
package com.dgex.offspring.application.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.ui.SendMessageWizard;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class SendMessageHandler {

  @CanExecute
  public boolean canExecute(IUserService userService) {
    for (IUser user : userService.getUsers()) {
      if (!user.getAccount().isReadOnly()) {
        return true;
      }
    }
    return false;
  }

  @Execute
  public void execute(Shell shell, INxtService nxt, IUserService userService,
      UISynchronize sync, IStylingEngine engine) {
    new WizardDialog(shell, new SendMessageWizard(userService, nxt, null, null))
        .open();
  }
		
}