/*******************************************************************************
 * Copyright (c) 2010 - 2013 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation Lars Vogel
 * <lars.Vogel@gmail.com> - Bug 419770
 *******************************************************************************/
package com.dgex.offspring.application.handlers;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.config.Config;
import com.dgex.offspring.messages.Messages;
import com.dgex.offspring.nxtCore.service.INxtService;

public class AboutHandler {

  @Execute
  public void execute(Shell shell, INxtService nxt) {
    MessageDialog.openInformation(shell, Messages.AboutHandler_title,
        formatMessage(nxt) + "\n\n" + formatUptime());
  }

  private String formatMessage(INxtService nxt) {
    return NLS.bind(Messages.AboutHandler_message, nxt.getSoftwareVersion());
  }

  private String formatUptime() {
    try {
      Duration duration = DatatypeFactory.newInstance().newDuration(
          System.currentTimeMillis() - Config.uptime);
      return "Application uptime "
          + String.format("%dh:%dm:%ds",
              duration.getDays() * 24 + duration.getHours(),
              duration.getMinutes(), duration.getSeconds());
    }
    catch (DatatypeConfigurationException e) {}
    return "";
  }

}
