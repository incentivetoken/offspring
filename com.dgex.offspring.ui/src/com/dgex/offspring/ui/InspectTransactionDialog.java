package com.dgex.offspring.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nxt.Constants;
import nxt.Nxt;
import nxt.Transaction;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;

import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.config.JSonWriter;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.Utils;
import com.dgex.offspring.ui.controls.TransactionTypes;
import com.dgex.offspring.user.service.IUserService;

public class InspectTransactionDialog extends TitleAreaDialog {

  static final Logger logger = Logger.getLogger(InspectTransactionDialog.class);
  private final List<Long> history = new ArrayList<Long>();
  private int historyCursor = 0;

  private Long transactionId;
  private Composite container;
  private Button previousButton;
  private Button nextButton;
  private Label timeLabel;
  private Link senderLink;
  private Link receiverLink;
  private Label heightLabel;
  private Label deadlineLabel;
  private Label amountLabel;
  private Label feeLabel;
  private Link blockLink;
  private Text hashText;
  private Text signatureText;
  private Text jsonText;
  private Link referencedLink;
  private final IStylingEngine engine;
  private final INxtService nxt;
  private final IUserService userService;
  private final UISynchronize sync;
  private final IContactsService contactsService;
  static InspectTransactionDialog INSTANCE = null;

  static final SimpleDateFormat dateFormat = new SimpleDateFormat(
      "dd MMM yy H:mm:ss");

  public InspectTransactionDialog(Shell shell, Long transactionId,
      INxtService nxt, IStylingEngine engine, IUserService userService,
      UISynchronize sync, IContactsService contactsService) {
    super(shell);
    this.transactionId = transactionId;
    this.engine = engine;
    this.nxt = nxt;
    this.userService = userService;
    this.sync = sync;
    this.contactsService = contactsService;
  }

  @Override
  protected void setShellStyle(int newShellStyle) {
    super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE
        | SWT.RESIZE);
    setBlockOnOpen(false);
  }

  @Override
  public void create() {
    super.create();
    setTitle("Transaction");
    setMessage(Convert.toUnsignedLong(transactionId));
  }

  /**
   * Static method that opens a new dialog or switches the existing dialog to
   * another account id. The dialog shows back and forward buttons to navigate
   * between accounts inspected.
   * 
   * @param transactionId
   * @return
   */
  public static void show(final Long transactionId, final INxtService nxt,
      final IStylingEngine engine, final IUserService userService,
      final UISynchronize sync, final IContactsService contactsService) {
    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        Shell shell = Display.getCurrent().getActiveShell();
        if (shell != null) {
          while (shell.getParent() != null) {
            shell = shell.getParent().getShell();
          }
        }
        if (INSTANCE == null) {
          INSTANCE = new InspectTransactionDialog(shell, transactionId, nxt,
              engine, userService, sync, contactsService);
          INSTANCE.history.add(transactionId);
          INSTANCE.historyCursor = 0;
          INSTANCE.open();
        }
        else {
          INSTANCE.history.add(transactionId);
          INSTANCE.historyCursor = INSTANCE.history.size() - 1;
          INSTANCE.setTransactionId(transactionId);
          INSTANCE.getShell().forceActive();
        }
      }
    });
  }

  @Override
  public boolean close() {
    INSTANCE = null;
    return super.close();
  }

  public void setTransactionId(Long transactionId) {
    this.transactionId = transactionId;
    Transaction transaction = Nxt.getBlockchain().getTransaction(transactionId);
    if (transaction == null) {
      logger.warn("Cannot open non-existing transaction");
      return;
    }

    setTitle("Transaction " + Convert.toUnsignedLong(transactionId));
    setMessage("Type: " + TransactionTypes.getTransactionType(transaction));

    updateNavigateButtons();

    timeLabel.setText(dateFormat.format(convertTimestamp(transaction
        .getTimestamp())) + " (" + transaction.getTimestamp() + ")");
    blockLink.setText("<A>" + Convert.toUnsignedLong(transaction.getBlockId())
        + "</A>");

    if (transaction.getReferencedTransactionId() != null)
      referencedLink.setText("<A>"
          + Convert.toUnsignedLong(transaction.getReferencedTransactionId())
          + "</A>");
    else
      referencedLink.setText("");

    senderLink.setText("<A>"
        + Convert.toUnsignedLong(transaction.getSenderId()) + "</A>");
    receiverLink.setText("<A>"
        + Convert.toUnsignedLong(transaction.getRecipientId()) + "</A>");
    amountLabel.setText(Utils.quantToString(transaction.getAmountNQT()));
    feeLabel.setText(Utils.quantToString(transaction.getFeeNQT()));
    heightLabel.setText(Integer.toString(transaction.getHeight()));
    deadlineLabel.setText(Integer.toString(transaction.getDeadline()));
    hashText.setText(transaction.getHash());
    signatureText.setText(Convert.toHexString(transaction.getSignature()));
    jsonText.setText(prettyPrint(transaction.getJSONObject()));

    timeLabel.pack();
    blockLink.pack();
    senderLink.pack();
    receiverLink.pack();
    amountLabel.pack();
    feeLabel.pack();
    heightLabel.pack();
    deadlineLabel.pack();

    container.layout();
  }

  private void updateNavigateButtons() {
    previousButton.setEnabled(historyCursor > 0);
    nextButton.setEnabled(historyCursor < (history.size() - 1));
  }

  private Date convertTimestamp(int timestamp) {
    return new Date((timestamp * 1000l) + (Constants.EPOCH_BEGINNING - 500));
  }

  private void labelData(Control control) {
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER)
        .applyTo(control);
  }

  private void fieldData(Control control) {
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER)
        .grab(true, false).applyTo(control);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    initializeDialogUnits(parent);
    Composite outerContainer = (Composite) super.createDialogArea(parent);

    GridLayout layout = new GridLayout(4, false);
    layout.horizontalSpacing = 15;
    layout.marginTop = 10;
    layout.marginLeft = 10;

    GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true);
    gd.widthHint = Math
        .round((convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH) / 2) * 3);

    container = new Composite(outerContainer, SWT.NONE);
    container.setLayoutData(gd);
    container.setLayout(layout);

    Label label = new Label(container, SWT.NONE);
    label.setText("Time");
    labelData(label);

    timeLabel = new Label(container, SWT.NONE);
    fieldData(timeLabel);

    Composite navigationComposite = new Composite(container, SWT.NONE);
    GridDataFactory.fillDefaults().span(2, 1).grab(true, false)
        .align(SWT.END, SWT.CENTER).applyTo(navigationComposite);
    navigationComposite.setLayout(new FillLayout());

    previousButton = new Button(navigationComposite, SWT.PUSH);
    previousButton.setEnabled(false);
    previousButton.setText("<");
    previousButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        historyCursor = Math.max(0, historyCursor - 1);
        setTransactionId(history.get(historyCursor));
      }
    });

    nextButton = new Button(navigationComposite, SWT.PUSH);
    nextButton.setEnabled(false);
    nextButton.setText(">");
    nextButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        historyCursor = Math.min(history.size() - 1, historyCursor + 1);
        setTransactionId(history.get(historyCursor));
      }
    });

    // -----

    label = new Label(container, SWT.NONE);
    label.setText("Sender");
    labelData(label);

    senderLink = new Link(container, SWT.NONE);
    fieldData(senderLink);
    senderLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Transaction t = Nxt.getBlockchain().getTransaction(transactionId);
        if (t != null) {
          InspectAccountDialog.show(t.getSenderId(), nxt, engine, userService,
              sync, contactsService);
        }
      }
    });

    label = new Label(container, SWT.NONE);
    label.setText("Receiver");
    labelData(label);

    receiverLink = new Link(container, SWT.NONE);
    fieldData(receiverLink);
    receiverLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Transaction t = Nxt.getBlockchain().getTransaction(transactionId);
        if (t != null) {
          InspectAccountDialog.show(t.getRecipientId(), nxt, engine,
              userService, sync, contactsService);
        }
      }
    });

    // -----

    label = new Label(container, SWT.NONE);
    label.setText("Block");
    labelData(label);

    blockLink = new Link(container, SWT.NONE);
    fieldData(blockLink);
    blockLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Transaction t = Nxt.getBlockchain().getTransaction(transactionId);
        if (t != null) {
          InspectBlockDialog.show(t.getBlockId(), nxt, engine, userService,
              sync, contactsService);
        }
      }
    });

    label = new Label(container, SWT.NONE);
    label.setText("Referenced");
    labelData(label);

    referencedLink = new Link(container, SWT.NONE);
    fieldData(referencedLink);

    // -----

    label = new Label(container, SWT.NONE);
    label.setText("Amount");
    labelData(label);

    amountLabel = new Label(container, SWT.NONE);
    fieldData(amountLabel);

    label = new Label(container, SWT.NONE);
    label.setText("Fee");
    labelData(label);

    feeLabel = new Label(container, SWT.NONE);
    fieldData(feeLabel);

    // -----

    label = new Label(container, SWT.NONE);
    label.setText("Height");
    labelData(label);

    heightLabel = new Label(container, SWT.NONE);
    fieldData(heightLabel);

    label = new Label(container, SWT.NONE);
    label.setText("Deadline");
    labelData(label);

    deadlineLabel = new Label(container, SWT.NONE);
    fieldData(deadlineLabel);

    // -----

    label = new Label(container, SWT.NONE);
    label.setText("Hash");
    labelData(label);

    hashText = new Text(container, SWT.BORDER);
    GridDataFactory.swtDefaults().span(3, 1).grab(true, false)
        .align(SWT.FILL, SWT.CENTER).applyTo(hashText);

    label = new Label(container, SWT.NONE);
    label.setText("Signature");
    labelData(label);

    signatureText = new Text(container, SWT.BORDER);
    GridDataFactory.swtDefaults().span(3, 1).grab(true, false)
        .align(SWT.FILL, SWT.CENTER).applyTo(signatureText);

    jsonText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP
        | SWT.V_SCROLL | SWT.H_SCROLL);

    GridDataFactory.swtDefaults().span(4, 1).grab(true, true)
        .align(SWT.FILL, SWT.CENTER).applyTo(jsonText);
    ((GridData) jsonText.getLayoutData()).minimumHeight = 220;

    sync.asyncExec(new Runnable() {

      @Override
      public void run() {
        setTransactionId(transactionId);
      }
    });

    return outerContainer;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL,
        true);
  }

  @Override
  protected boolean isResizable() {
    return true;
  }

  private String prettyPrint(JSONObject object) {
    JSonWriter writer = new JSonWriter();
    try {
      object.writeJSONString(writer);
      return writer.toString();
    }
    catch (IOException e) {
      logger.error("Could not serialize JSON", e);
    }
    finally {
      try {
        writer.close();
      }
      catch (IOException e) {}
    }
    return "ERROR";
  }
}
