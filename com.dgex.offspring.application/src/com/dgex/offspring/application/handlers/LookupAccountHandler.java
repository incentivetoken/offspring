package com.dgex.offspring.application.handlers;

import nxt.Account;
import nxt.util.Convert;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.dgex.offspring.config.ContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.ui.InspectAccountDialog;
import com.dgex.offspring.user.service.IUserService;

public class LookupAccountHandler {

  class AccountValidator implements IInputValidator {

    @Override
    public String isValid(String text) {
      text = text.trim();
      if (text == null || text.isEmpty()) {
        return "Invalid Account Number";
      }

      Long id = Convert.parseUnsignedLong(text);
      if (id == null) {
        return "Invalid Account Number";
      }

      Account account = Account.getAccount(id);
      if (account == null) {
        return "That account does not exist";
      }
      return null;
    }
  }

  @Execute
  public void execute(INxtService nxt, IStylingEngine engine,
      IUserService userService, UISynchronize sync) {

    InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(),
        "Lookup Account", "Enter account number", "", new AccountValidator());
    if (dialog.open() == Window.OK) {
      Long id = Convert.parseUnsignedLong(dialog.getValue());
      InspectAccountDialog.show(id, nxt, engine, userService, sync,
          ContactsService.getInstance());
    }
  }
}