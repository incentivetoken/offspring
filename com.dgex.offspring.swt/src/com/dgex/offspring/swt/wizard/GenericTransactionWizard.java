package com.dgex.offspring.swt.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class GenericTransactionWizard extends Wizard {

  static Image errorImage = FieldDecorationRegistry.getDefault()
      .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

  private final Map<String, ControlDecoration> decorators = new HashMap<String, ControlDecoration>();

  class CreatePage extends WizardPage {

    private final IGenericTransaction transaction;
    public boolean _canFlipToNextPage = false;

    protected CreatePage(IGenericTransaction transaction) {
      super("page1");
      this.transaction = transaction;
      setTitle(getWindowTitle());
      setMessage("Step 1 of 2 - Enter Transaction Details");
    }

    @Override
    public void createControl(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10)
          .spacing(20, 5).applyTo(composite);

      /* generate the wizard fields */
      for (IGenericTransactionField field : transaction.getFields()) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(field.getLabel());

        Control control = field.createControl(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
            .grab(true, false).applyTo(control);

        ControlDecoration deco = new ControlDecoration(control, SWT.TOP
            | SWT.RIGHT);
        deco.setImage(errorImage);
        deco.hide();
        decorators.put(field.getLabel(), deco);
      }
      setControl(composite);
    }

    @Override
    public boolean canFlipToNextPage() {
      return _canFlipToNextPage;
    }

    @Override
    public boolean isPageComplete() {
      return canFlipToNextPage();
    }
  };

  class SendPage extends WizardPage {

    private final IGenericTransaction transaction;
    private boolean can_finish = false;

    protected SendPage(IGenericTransaction transaction) {
      super("page2");
      this.transaction = transaction;
      setTitle(getWindowTitle());
      setMessage("Step 2 of 2 - Confirm and send transaction");
    }

    @Override
    public void createControl(final Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10)
          .spacing(20, 5).applyTo(composite);

      /* generate the wizard fields */
      for (IGenericTransactionField field : transaction.getFields()) {
        Label label = new Label(composite, SWT.NONE);
        label.setText(field.getLabel());

        Control control = field.createReadonlyControl(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
            .grab(true, false).applyTo(control);

        ControlDecoration deco = new ControlDecoration(control, SWT.TOP
            | SWT.RIGHT);
        deco.setImage(errorImage);
        deco.hide();
      }

      /* transaction id and send button */
      new Label(composite, SWT.NONE);
      Composite sendComp = new Composite(composite, SWT.NONE);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
          .grab(true, false).applyTo(sendComp);
      GridLayoutFactory.fillDefaults().numColumns(2).spacing(5, 5)
          .applyTo(sendComp);

      Button sendButton = new Button(sendComp, SWT.PUSH);
      sendButton.setText("Send");
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
          .applyTo(sendButton);

      final Text idText = new Text(sendComp, SWT.BORDER);
      idText.setMessage("transaction id");
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
          .grab(true, false).applyTo(idText);

      /* status label */
      new Label(composite, SWT.NONE);
      final Label statusLabel = new Label(composite, SWT.NONE);
      statusLabel.setText("TRANSACTION NOT SEND");
      statusLabel.setFont(JFaceResources.getFontRegistry().getBold(""));

      sendButton.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          statusLabel.setText("PLEASE WAIT");
          parent.getDisplay().timerExec(500, new Runnable() {

            @Override
            public void run() {
              String[] message = new String[1];
              String id = transaction.sendTransaction(message);
              if (id != null) {
                idText.setText(id);
                statusLabel.setText("TRANSACTION SEND SUCCESS");

                can_finish = true;
                try {
                  getContainer().updateButtons();
                }
                catch (Exception e) {}
              }
              else {
                statusLabel.setText(message[0]);
              }
              statusLabel.pack();
              parent.layout();
            }
          });
        }
      });

      setControl(composite);
    }

    @Override
    public boolean isPageComplete() {
      return can_finish;
    }
  };

  public final IGenericTransactionField fieldSender = new IGenericTransactionField() {

    private final List<IUser> senders = new ArrayList<IUser>();
    private Combo comboSender;
    private Combo comboSenderReadonly;

    @Override
    public String getLabel() {
      return "Sender";
    }

    @Override
    public Object getValue() {
      return senders.get(comboSender.getSelectionIndex());
    }

    @Override
    public Control createControl(Composite parent) {
      comboSender = new Combo(parent, SWT.READ_ONLY);
      for (IUser user : userService.getUsers()) {
        if (!user.getAccount().isReadOnly()) {
          senders.add(user);
          comboSender.add(createLabel(user));
        }
      }

      int index = senders.indexOf(userService.getActiveUser());
      index = index == -1 ? 0 : index;
      user = senders.get(index);
      comboSender.select(index);

      comboSender.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          user = senders.get(comboSender.getSelectionIndex());
          requestVerification();
        }
      });
      return comboSender;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      comboSenderReadonly = new Combo(parent, SWT.READ_ONLY);
      comboSenderReadonly.add(createLabel(user));
      comboSenderReadonly.select(0);
      return comboSenderReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      comboSenderReadonly.removeAll();
      comboSenderReadonly.add(createLabel(user));
      comboSenderReadonly.select(0);
      return transaction.verifySender(message);
    }

    private String createLabel(IUser user) {
      return "# " + user.getAccount().getStringId() + " " + user.getName();
    }
  };

  private IGenericTransaction transaction;
  private CreatePage createPage;
  private SendPage sendPage;
  private final IUserService userService;
  protected IUser user = null;

  public GenericTransactionWizard(IUserService userService) {
    super();
    setWindowTitle("Transaction");
    this.userService = userService;
  }

  public void setTransaction(IGenericTransaction transaction) {
    this.transaction = transaction;
    this.createPage = new CreatePage(transaction);
    this.sendPage = new SendPage(transaction);
  }

  @Override
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
    requestVerification();
  }

  public void requestVerification() {
    boolean verified = true;
    for (IGenericTransactionField field : transaction.getFields()) {
      String[] message = new String[1];
      ControlDecoration deco = decorators.get(field.getLabel());
      if (field.verify(message)) {
        deco.hide();
      }
      else {
        deco.setDescriptionText(message[0]);
        deco.show();
        verified = false;
      }
    }

    createPage._canFlipToNextPage = verified;
    try {
      getContainer().updateButtons();
    }
    catch (Exception e) {}
  }

  public void setUser(IUser user) {
    this.user = user;
    requestVerification();
  }

  @Override
  public void addPages() {
    addPage(createPage);
    addPage(sendPage);
  }

  @Override
  public boolean performFinish() {
    return true;
  }
}
