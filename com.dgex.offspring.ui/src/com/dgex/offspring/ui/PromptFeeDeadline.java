package com.dgex.offspring.ui;

import nxt.Constants;
import nxt.util.Convert;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PromptFeeDeadline extends Dialog {

  static Image errorImage = FieldDecorationRegistry.getDefault()
      .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

  private Text feeText;
  private Text deadlineText;
  private ControlDecoration decoFee;
  private ControlDecoration decoDeadline;
  private long feeNQT = 1 * Constants.ONE_NXT;
  private long minimumFeeNQT = 1 * Constants.ONE_NXT;
  private short deadline = 1440;

  public PromptFeeDeadline(Shell shell) {
    super(shell);
  }

  public void setMinimumFeeNQT(long feeNQT) {
    this.minimumFeeNQT = feeNQT;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, false);
    layout.marginRight = 5;
    layout.marginLeft = 10;
    container.setLayout(layout);

    Label feeLabel = new Label(container, SWT.NONE);
    feeLabel.setText("Fee");

    feeText = new Text(container, SWT.BORDER);
    feeText
        .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    feeText.setText(Convert.toNXT(minimumFeeNQT));

    decoFee = new ControlDecoration(feeText, SWT.TOP | SWT.RIGHT);
    decoFee.setImage(errorImage);
    decoFee.hide();

    Label deadlineLabel = new Label(container, SWT.NONE);
    deadlineLabel.setText("Deadline");

    deadlineText = new Text(container, SWT.BORDER);
    deadlineText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
        1, 1));
    deadlineText.setText(Integer.toString(deadline));

    decoDeadline = new ControlDecoration(deadlineText, SWT.TOP | SWT.RIGHT);
    decoDeadline.setImage(errorImage);
    decoDeadline.hide();

    ModifyListener listener = new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null) {
          button.setEnabled(verifyInput());
        }
      }
    };
    feeText.addModifyListener(listener);
    deadlineText.addModifyListener(listener);

    return container;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
        true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        IDialogConstants.CANCEL_LABEL, false);
  }

  public void setFeeNQT(long feeNQT) {
    this.feeNQT = feeNQT;
  }

  public long getFeeNQT() {
    return this.feeNQT;
  }

  public void setDeadline(short deadline) {
    this.deadline = deadline;
  }

  public short getDeadline() {
    return this.deadline;
  }

  private boolean verifyInput() {
    boolean verified = true;
    if (!isNXT(feeText.getText())) {
      decoFee.setDescriptionText("Fee must be numeric");
      decoFee.show();
      verified = false;
    }
    else {
      feeNQT = Convert.parseNXT(feeText.getText());
      if ((feeNQT <= Constants.ONE_NXT)
          || (feeNQT >= Constants.MAX_BALANCE_NQT)) {
        decoFee.setDescriptionText("Must be at least 1");
        decoFee.show();
        verified = false;
      }
      else if (feeNQT < minimumFeeNQT) {
        decoFee.setDescriptionText("Must be at least "
            + Convert.toNXT(minimumFeeNQT));
        decoFee.show();
        verified = false;
      }
      else {
        decoFee.hide();
      }
    }
    if (!isShort(deadlineText.getText())) {
      decoDeadline.setDescriptionText("Deadline must be numeric");
      decoDeadline.show();
      verified = false;
    }
    else {
      deadline = Short.parseShort(deadlineText.getText());
      if ((deadline < 1) || (deadline > 1440)) {
        decoDeadline.setDescriptionText("Deadline must be between 1 and 1440");
        decoDeadline.show();
        verified = false;
      }
      else {
        decoDeadline.hide();
      }
    }
    return verified;
  }

  private static boolean isNXT(String str) {
    try {
      Convert.parseNXT(str);
    }
    catch (Exception nfe) {
      return false;
    }
    return true;
  }

  private static boolean isShort(String str) {
    try {
      Short.parseShort(str);
    }
    catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }
}
