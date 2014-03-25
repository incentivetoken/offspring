package com.dgex.offspring.application.ui.messages;

import java.io.UnsupportedEncodingException;

import nxt.Constants;
import nxt.NxtException.ValidationException;
import nxt.util.Convert;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.nxtCore.core.AccountHelper;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;
import com.dgex.offspring.ui.PromptFeeDeadline;
import com.dgex.offspring.user.service.IUser;

public class MessagesControl extends Composite {

  static Image errorImage = FieldDecorationRegistry.getDefault()
      .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

  private final IUser user;
  private final INxtService nxt;
  private Text messageRecipientText;
  private ControlDecoration decoMessageRecipientText;
  private Text messageMessageText;

  private ControlDecoration decoMessageMessageText;

  private Button messageSendButton;

  private final MessagesTableViewer messagesViewer;

  public MessagesControl(Composite parent, int style, IUser user,
      INxtService nxt) {

    super(parent, style);
    this.user = user;
    this.nxt = nxt;
    GridLayoutFactory.fillDefaults().spacing(10, 5).numColumns(3).applyTo(this);

    /* top bar (recipient, message, send) */

    if (!user.getAccount().isReadOnly()) {

      messageRecipientText = new Text(this, SWT.BORDER);
      GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
          .hint(150, SWT.DEFAULT).applyTo(messageRecipientText);
      messageRecipientText.setMessage("recipient");

      decoMessageRecipientText = new ControlDecoration(messageRecipientText,
          SWT.TOP | SWT.RIGHT);
      decoMessageRecipientText.setImage(errorImage);
      decoMessageRecipientText.hide();

      messageMessageText = new Text(this, SWT.BORDER);
      GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
          .grab(true, false).applyTo(messageMessageText);
      messageMessageText.setMessage("message");

      decoMessageMessageText = new ControlDecoration(messageMessageText,
          SWT.TOP | SWT.RIGHT);
      decoMessageMessageText.setImage(errorImage);
      decoMessageMessageText.hide();

      messageSendButton = new Button(this, SWT.PUSH);
      GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
          .applyTo(messageSendButton);

      messageSendButton.setText("Send");
      messageSendButton.setEnabled(false);

      hookupMessageControls();
    }

    /* bottom bar table viewer */

    messagesViewer = new MessagesTableViewer(this, user, nxt);
    GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .span(3, 1).applyTo(messagesViewer.getControl());
  }

  public void refresh() {
    messagesViewer.refresh();
  }

  private void hookupMessageControls() {
    messageRecipientText.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        messageSendButton.setEnabled(verifyMessage());
      }
    });

    messageMessageText.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        messageSendButton.setEnabled(verifyMessage());
      }
    });

    messageSendButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        try {
          sendMessage();
        }
        catch (UnsupportedEncodingException e1) {
          MessageDialog.openError(getShell(), "Transaction Exception",
              e1.getMessage());
        }
      }
    });
  }

  private void sendMessage() throws UnsupportedEncodingException {
    PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
    if (dialog.open() != Window.OK) {
      return;
    }

    final int fee = dialog.getFee();
    final short deadline = dialog.getDeadline();
    final Long recipient = Convert.parseUnsignedLong(messageRecipientText
        .getText().trim());
    final byte[] message = messageMessageText.getText().trim()
        .getBytes("UTF-8");

    BusyIndicator.showWhile(getDisplay(), new Runnable() {

      @Override
      public void run() {
        try {
          nxt.createSendMessageTransaction(user.getAccount(),
              new AccountHelper(nxt, recipient), message, deadline, fee, 0l);
          messagesViewer.refresh();
        }
        catch (ValidationException e) {
          MessageDialog.openError(getShell(), "Validation Exception",
              e.getMessage());
        }
        catch (TransactionException e) {
          MessageDialog.openError(getShell(), "Transaction Exception",
              e.getMessage());
        }
      }
    });
  }

  private boolean verifyMessage() {
    boolean verified = true;
    decoMessageMessageText.setDescriptionText("");
    decoMessageMessageText.hide();

    byte[] message = new byte[0];
    try {
      message = messageMessageText.getText().trim().getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      decoMessageMessageText.setDescriptionText("UTF-8 not supported");
      decoMessageMessageText.show();
      verified = false;
    }

    if (message.length == 0) {
      decoMessageMessageText.setDescriptionText("Message length is zero");
      decoMessageMessageText.show();
      verified = false;
    }

    if (message.length > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
      decoMessageMessageText.setDescriptionText("Message to long");
      decoMessageMessageText.show();
      verified = false;
    }

    if (messageMessageText.getText().trim().isEmpty()) {
      decoMessageMessageText.setDescriptionText("Message cannot be empty");
      decoMessageMessageText.show();
      verified = false;
    }

    if (messageRecipientText.getText().trim().isEmpty()) {
      decoMessageRecipientText.setDescriptionText("Recipient cannot be empty");
      decoMessageRecipientText.show();
      verified = false;
    }

    try {
      Convert.parseUnsignedLong(messageRecipientText.getText().trim());
    }
    catch (IllegalArgumentException e) {
      decoMessageRecipientText.setDescriptionText("Invalid Recipient");
      decoMessageRecipientText.show();
      verified = false;
    }
    return verified;
  }

}
