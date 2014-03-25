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

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.swt.widgets.Display;

import com.dgex.offspring.application.utils.Shutdown;
import com.dgex.offspring.dataprovider.service.IDataProviderPool;
import com.dgex.offspring.nxtCore.service.INxtService;

public class QuitHandler {

  @Execute
  public void execute(Display display, IEventBroker broker, INxtService nxt,
      IDataProviderPool pool, IWorkbench workbench, UISynchronize sync) {
    if (Shutdown.execute(display.getActiveShell(), broker, sync, nxt, pool)) {
      workbench.close();
    }
  }

}
