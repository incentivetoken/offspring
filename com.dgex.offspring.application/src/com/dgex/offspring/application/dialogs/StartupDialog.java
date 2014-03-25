package com.dgex.offspring.application.dialogs;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class StartupDialog extends Dialog {

  static Logger logger = Logger.getLogger(StartupDialog.class);

  private Composite mainContainer = null;
  private Label messageLabel = null;
  private ProgressIndicator progressBar = null;
  private final IProgressMonitor monitor;
  private boolean showOKButton = true;

  public StartupDialog(Shell shell, UISynchronize sync) {
    super(shell);
    this.monitor = new StartupProgressMonitor(this, sync);
  }

  public IProgressMonitor getProgressMonitor() {
    return monitor;
  }

  public void setStatus(String status) {
    if (messageLabel != null && !messageLabel.isDisposed()) {
      messageLabel.setText(status);
      messageLabel.pack();
      mainContainer.layout();
    }
  }

  public void setError(String error) {
    if (messageLabel != null && !messageLabel.isDisposed()) {
      messageLabel.setText("ERROR " + error);
      messageLabel.pack();
      mainContainer.layout();
    }
  }

  public ProgressIndicator getProgressIndicator() {
    return progressBar;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    if (showOKButton)
      createButton(parent, IDialogConstants.OK_ID, "Run In Background", true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        showOKButton ? IDialogConstants.CANCEL_LABEL
            : "Force Quit (Not Recommended)", false);
  }

  @Override
  protected void buttonPressed(int id) {
    if (id == IDialogConstants.CANCEL_ID) {
      if (showOKButton) { // dialog is used for startup
        boolean exit = MessageDialog.openConfirm(getShell(), "Exit Offspring?",
            "Do you want to exit Offspring?");
        if (!exit)
          return;
      }
      monitor.setCanceled(true);
    }
    super.buttonPressed(id);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);

    GridLayout layout = new GridLayout(1, false);
    layout.horizontalSpacing = 15;
    layout.marginTop = 10;
    layout.marginLeft = 10;

    GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true);
    gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);

    mainContainer = new Composite(container, SWT.NONE);
    mainContainer.setLayoutData(gd);
    mainContainer.setLayout(layout);

    messageLabel = new Label(mainContainer, SWT.WRAP);
    messageLabel.setText("Initializing");
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false)
        .applyTo(messageLabel);

    new Label(mainContainer, SWT.NONE);

    progressBar = new ProgressIndicator(mainContainer, SWT.SMOOTH);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false)
        .applyTo(progressBar);

    mainContainer.layout();
    return container;
  }

  public void showOKButton(boolean b) {
    showOKButton = b;
  }
}
