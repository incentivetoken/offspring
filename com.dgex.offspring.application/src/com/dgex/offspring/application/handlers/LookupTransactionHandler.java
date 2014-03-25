package com.dgex.offspring.application.handlers;

import nxt.Nxt;
import nxt.Transaction;
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
import com.dgex.offspring.ui.InspectTransactionDialog;
import com.dgex.offspring.user.service.IUserService;

public class LookupTransactionHandler {

  class TransactionValidator implements IInputValidator {

    @Override
    public String isValid(String text) {
      text = text.trim();
      if (text == null || text.isEmpty()) {
        return "Invalid Transaction Id";
      }

      Long id = Convert.parseUnsignedLong(text);
      if (id == null) {
        return "Invalid Transaction Id";
      }

      Transaction transaction = Nxt.getBlockchain().getTransaction(id);
      if (transaction == null) {
        return "That transaction does not exist";
      }
      return null;
    }
  }

  @Execute
  public void execute(INxtService nxt, IStylingEngine engine,
      IUserService userService, UISynchronize sync) {

    InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(),
        "Lookup Transaction", "Enter transaction id", "",
        new TransactionValidator());
    if (dialog.open() == Window.OK) {
      Long id = Convert.parseUnsignedLong(dialog.getValue());
      InspectTransactionDialog.show(id, nxt, engine, userService, sync,
          ContactsService.getInstance());
    }
  }

}