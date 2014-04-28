package com.dgex.offspring.application.dialogs;

import java.util.List;

import nxt.Account;
import nxt.Token;
import nxt.util.Convert;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.application.utils.Layouts;
import com.dgex.offspring.config.Formatter;
import com.dgex.offspring.messages.Messages;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.Utils;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class AuthTokenDialog extends TitleAreaDialog {

  private IUser activeUser = null;
  private final INxtService nxt;
  private final IUserService userService;

  private int defaultHeight;

  private Combo comboSender;
  private Text textWebsite;
  private Text textToken;
  private Text textMyToken;
  private Text textMyWebsite;
  private Label labelStatus;

  private Text textAccount;

  public AuthTokenDialog(Shell shell, INxtService nxt, IUserService userService) {
    super(shell);
    this.nxt = nxt;
    this.userService = userService;
  }

  @Override
  public void create() {
    super.create();
    setTitle(Messages.AuthTokenDialog_title);
    setMessage(Messages.AuthTokenDialog_message_default);
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL,
        true);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);

    GridData data = Layouts.Grid.fill(false, false);
    data.widthHint = 300;

    Composite mainContainer = new Composite(container, SWT.NONE);
    mainContainer.setLayout(Layouts.Grid.create(1, 0, 0, 0, 0, 0, 3));
    mainContainer.setLayoutData(data);

    Group createTokenGroup = new Group(mainContainer, SWT.NONE);
    createTokenGroup.setText(Messages.AuthTokenDialog_label_generate_tokens);
    createTokenGroup.setLayoutData(new GridData(GridData.FILL,
        GridData.BEGINNING, true, false));
    createTokenGroup.setLayout(Layouts.Grid.create(1, 5, 5, 5, 5, 0, 3));

    Group verifyTokenGroup = new Group(mainContainer, SWT.NONE);
    verifyTokenGroup.setText(Messages.AuthTokenDialog_label_decode_tokens);
    verifyTokenGroup.setLayoutData(new GridData(GridData.FILL,
        GridData.BEGINNING, true, false));
    verifyTokenGroup.setLayout(Layouts.Grid.create(1, 5, 5, 5, 5, 0, 3));

    /* Create token group */

    comboSender = new Combo(createTokenGroup, SWT.READ_ONLY);
    comboSender.setLayoutData(Layouts.Grid.fill(true, false));

    textMyWebsite = new Text(createTokenGroup, SWT.BORDER);
    textMyWebsite.setLayoutData(Layouts.Grid.fill(true, false));
    textMyWebsite.setMessage(Messages.AuthTokenDialog_label_website);

    defaultHeight = textMyWebsite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

    data = Layouts.Grid.fill(true, false);
    data.heightHint = 3 * defaultHeight;

    textMyToken = new Text(createTokenGroup, SWT.MULTI | SWT.BORDER | SWT.WRAP
        | SWT.V_SCROLL);
    textMyToken.setLayoutData(data);
    textMyToken.setEditable(false);

    /* Verify token group */

    // textExpectedAccount = new Text(verifyTokenGroup, SWT.BORDER);
    // textExpectedAccount.setLayoutData(Layouts.Grid.fill(true, false));
    // textExpectedAccount.setMessage("expected account");

    textWebsite = new Text(verifyTokenGroup, SWT.BORDER);
    textWebsite.setLayoutData(Layouts.Grid.fill(true, false));
    textWebsite.setMessage(Messages.AuthTokenDialog_label_website);

    data = Layouts.Grid.fill(true, false);
    data.heightHint = 3 * defaultHeight;

    textToken = new Text(verifyTokenGroup, SWT.MULTI | SWT.BORDER | SWT.WRAP
        | SWT.V_SCROLL);
    textToken.setLayoutData(data);
    textToken.setMessage(Messages.AuthTokenDialog_label_token);

    textAccount = new Text(verifyTokenGroup, SWT.BORDER);
    textAccount.setLayoutData(Layouts.Grid.fill(true, false));
    textAccount.setMessage(Messages.AuthTokenDialog_label_actual_account);
    textAccount.setEditable(false);

    /* Main status label */

    labelStatus = new Label(mainContainer, SWT.NONE);
    labelStatus.setFont(JFaceResources.getFontRegistry().getBold("")); //$NON-NLS-1$
    labelStatus.setLayoutData(Layouts.Grid.fill(true, false));
    labelStatus.setText(" "); //$NON-NLS-1$

    setupAccountCombo();
    setupControls();

    return container;
  }

  private void setStatus(String status) {
    if (labelStatus != null && !labelStatus.isDisposed())
      labelStatus.setText(status);
  }

  /* Populate the sender combo box */
  private void setupAccountCombo() {
    if (userService.getUsers().size() == 0)
      return;

    int selected_index = 0;
    List<IUser> users = userService.getUsers();
    for (int i = 0; i < users.size(); i++) {
      IUser user = users.get(i);
      comboSender.add(user.getAccount().getStringId() + " "
          + Utils.quantToString(user.getAccount().getBalanceNQT()) + " "
          + user.getName());
      if (user.equals(userService.getActiveUser())) {
        selected_index = i;
        activeUser = user;
      }
    }

    // there are no logged in users
    if (comboSender.getItemCount() == 0) {
      comboSender.add(Messages.AuthTokenDialog_label_unlock_first);
      comboSender.select(comboSender.getItemCount() - 1);
    }
    // there is no activeUser (not sure if possible)
    else if (activeUser == null) {
      comboSender.add(""); //$NON-NLS-1$
      comboSender.select(comboSender.getItemCount() - 1);
    }
    else {
      comboSender.select(selected_index);
    }

    if (comboSender.getItemCount() > 0) {
      comboSender.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          if (comboSender.getSelectionIndex() < userService.getUsers().size()) {
            activeUser = userService.getUsers().get(
                comboSender.getSelectionIndex());
          }
          else {
            activeUser = null;
          }
        }
      });
    }
  }

  private void setupControls() {
    textMyWebsite.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        textMyToken.setText(nxt.generateAuthorizationToken(activeUser
            .getAccount().getPrivateKey(), textMyWebsite.getText()));
      }
    });

    ModifyListener listener = new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        Token token = nxt.getToken(textWebsite.getText(), textToken.getText());
        String account = Convert.toUnsignedLong(Account.getId(token
            .getPublicKey()));
        textAccount.setText(account);

        if (token.isValid())
          setStatus(Messages.AuthTokenDialog_status_token_valid
              + Formatter.formatTimestampLocale(Integer.valueOf(
                  token.getTimestamp()).longValue()));
        else
          setStatus(Messages.AuthTokenDialog_status_token_not_valid);
      }
    };

    textWebsite.addModifyListener(listener);
    textToken.addModifyListener(listener);
  }
}
