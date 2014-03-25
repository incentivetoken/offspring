package com.dgex.offspring.application.handlers;

import nxt.Block;
import nxt.Nxt;
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
import com.dgex.offspring.ui.InspectBlockDialog;
import com.dgex.offspring.user.service.IUserService;

public class LookupBlockHandler {

  class BlockValidator implements IInputValidator {

    @Override
    public String isValid(String text) {
      text = text.trim();
      if (text == null || text.isEmpty()) {
        return "Invalid Block Id";
      }

      Long id = Convert.parseUnsignedLong(text);
      if (id == null) {
        return "Invalid Block Id";
      }

      Block block = Nxt.getBlockchain().getBlock(id);
      if (block == null) {
        return "That block does not exist";
      }
      return null;
    }
  }

  @Execute
  public void execute(INxtService nxt, IStylingEngine engine,
      IUserService userService, UISynchronize sync) {

    InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(),
        "Lookup Block", "Enter block id", "", new BlockValidator());
    if (dialog.open() == Window.OK) {
      Long id = Convert.parseUnsignedLong(dialog.getValue());
      InspectBlockDialog.show(id, nxt, engine, userService, sync,
          ContactsService.getInstance());
    }
  }

}