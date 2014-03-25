package com.dgex.offspring.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nxt.Block;
import nxt.Constants;
import nxt.Nxt;
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
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;

import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.config.JSonWriter;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.user.service.IUserService;

public class InspectBlockDialog extends TitleAreaDialog {

  static final Logger logger = Logger.getLogger(InspectBlockDialog.class);
  private final List<Long> history = new ArrayList<Long>();
  private int historyCursor = 0;

  private Long blockId;
  private Composite container;
  private Button previousButton;
  private Button nextButton;
  private Label timeLabel;
  private Link generatorLink;
  private Label heightLabel;
  private Label amountLabel;
  private Label feeLabel;
  private Text hashText;
  private Text jsonText;
  private Label baseTargetLabel;
  private Label difficultyLabel;
  private Text generationSignatureText;
  private Text blockSignatureText;
  private BlockTransactionViewer viewer;
  private TabFolder tabFolder;
  private TabItem jsonTab;
  private TabItem transactionsTab;

  private final IStylingEngine engine;
  private final INxtService nxt;
  private final IUserService userService;
  private final UISynchronize sync;
  private final IContactsService contactsService;
  static InspectBlockDialog INSTANCE = null;

  static final SimpleDateFormat dateFormat = new SimpleDateFormat(
      "dd MMM yy hh:mm:ss");

  public InspectBlockDialog(Shell shell, Long blockId, INxtService nxt,
      IStylingEngine engine, IUserService userService, UISynchronize sync,
      IContactsService contactsService) {
    super(shell);
    this.blockId = blockId;
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
    setTitle("Block " + Convert.toUnsignedLong(blockId));
    setMessage("");
  }

  /**
   * Static method that opens a new dialog or switches the existing dialog to
   * another block id. The dialog shows back and forward buttons to navigate
   * between accounts inspected.
   * 
   * @param blockId
   * @return
   */
  public static void show(final Long blockId, final INxtService nxt,
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
          INSTANCE = new InspectBlockDialog(shell, blockId, nxt, engine,
              userService, sync, contactsService);
          INSTANCE.history.add(blockId);
          INSTANCE.historyCursor = 0;
          INSTANCE.open();
        }
        else {
          INSTANCE.history.add(blockId);
          INSTANCE.historyCursor = INSTANCE.history.size() - 1;
          INSTANCE.setBlockId(blockId);
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

  public void setBlockId(Long blockId) {
    this.blockId = blockId;
    Block block = Nxt.getBlockchain().getBlock(blockId);
    if (block == null) {
      logger.warn("Cannot open non-existing block");
      return;
    }

    setTitle("Block " + Convert.toUnsignedLong(blockId));
    setMessage("Contains " + block.getTransactionIds().size()
        + " transactions (v" + block.getVersion() + ")");

    updateNavigateButtons();

    timeLabel.setText(dateFormat.format(convertTimestamp(block.getTimestamp()))
        + " (" + block.getTimestamp() + ")");
    generatorLink.setText("<A>"
        + Convert.toUnsignedLong(block.getGeneratorId()) + "</A>");
    baseTargetLabel.setText(Long.toString(block.getBaseTarget()));
    heightLabel.setText(Integer.toString(block.getHeight()));
    difficultyLabel.setText(block.getCumulativeDifficulty().toString());
    amountLabel.setText(Integer.toString(block.getTotalAmount()));
    feeLabel.setText(Integer.toString(block.getTotalFee()));
    hashText.setText(Convert.toHexString(block.getPayloadHash()));
    generationSignatureText.setText(Convert.toHexString(block
        .getGenerationSignature()));
    blockSignatureText.setText(Convert.toHexString(block.getBlockSignature()));

    viewer.setInput(blockId);
    jsonText.setText(prettyPrint(block.getJSONObject()));

    timeLabel.pack();
    generatorLink.pack();
    baseTargetLabel.pack();
    heightLabel.pack();
    difficultyLabel.pack();
    amountLabel.pack();
    feeLabel.pack();
    hashText.pack();
    generationSignatureText.pack();
    blockSignatureText.pack();

    container.layout();
  }

  private void updateNavigateButtons() {
    previousButton.setEnabled(historyCursor > 0);
    nextButton.setEnabled(historyCursor < (history.size() - 1));
  }

  private Date convertTimestamp(int timestamp) {
    return new Date((timestamp * 1000l) + (Constants.EPOCH_BEGINNING - 500L));
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
        setBlockId(history.get(historyCursor));
      }
    });

    nextButton = new Button(navigationComposite, SWT.PUSH);
    nextButton.setEnabled(false);
    nextButton.setText(">");
    nextButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        historyCursor = Math.min(history.size() - 1, historyCursor + 1);
        setBlockId(history.get(historyCursor));
      }
    });

    // -----

    label = new Label(container, SWT.NONE);
    label.setText("Generator");
    labelData(label);

    generatorLink = new Link(container, SWT.NONE);
    fieldData(generatorLink);
    generatorLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Block block = Nxt.getBlockchain().getBlock(blockId);
        if (block != null) {
          InspectAccountDialog.show(block.getGeneratorId(), nxt, engine,
              userService, sync, contactsService);
        }
      }
    });

    label = new Label(container, SWT.NONE);
    label.setText("Base Target");
    labelData(label);

    baseTargetLabel = new Label(container, SWT.NONE);
    fieldData(baseTargetLabel);

    // -----

    label = new Label(container, SWT.NONE);
    label.setText("Height");
    labelData(label);

    heightLabel = new Label(container, SWT.NONE);
    fieldData(heightLabel);

    label = new Label(container, SWT.NONE);
    label.setText("Difficulty");
    labelData(label);

    difficultyLabel = new Label(container, SWT.NONE);
    fieldData(difficultyLabel);

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
    // payload hash
    label = new Label(container, SWT.NONE);
    label.setText("Hash");
    labelData(label);

    hashText = new Text(container, SWT.BORDER);
    GridDataFactory.swtDefaults().span(3, 1).grab(true, false)
        .align(SWT.FILL, SWT.CENTER).applyTo(hashText);

    // -----
    // generation signature
    label = new Label(container, SWT.NONE);
    label.setText("Generation Signature");
    labelData(label);

    generationSignatureText = new Text(container, SWT.BORDER);
    GridDataFactory.swtDefaults().span(3, 1).grab(true, false)
        .align(SWT.FILL, SWT.CENTER).applyTo(generationSignatureText);

    // -----
    // generation signature
    label = new Label(container, SWT.NONE);
    label.setText("Block Signature");
    labelData(label);

    blockSignatureText = new Text(container, SWT.BORDER);
    GridDataFactory.swtDefaults().span(3, 1).grab(true, false)
        .align(SWT.FILL, SWT.CENTER).applyTo(blockSignatureText);

    // -----

    tabFolder = new TabFolder(container, SWT.NONE);

    transactionsTab = new TabItem(tabFolder, SWT.NONE);
    transactionsTab.setText("Transactions");

    jsonTab = new TabItem(tabFolder, SWT.NONE);
    jsonTab.setText("JSON");

    viewer = new BlockTransactionViewer(tabFolder, 0l, contactsService, nxt,
        engine, userService, sync);
    transactionsTab.setControl(viewer.getControl());

    jsonText = new Text(tabFolder, SWT.MULTI | SWT.BORDER | SWT.WRAP
        | SWT.V_SCROLL | SWT.H_SCROLL);
    jsonTab.setControl(jsonText);

    Point size = jsonText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    GridDataFactory.swtDefaults().span(4, 1).grab(true, true)
        .align(SWT.BEGINNING, SWT.CENTER).applyTo(tabFolder);
    ((GridData) tabFolder.getLayoutData()).minimumHeight = 220;

    sync.asyncExec(new Runnable() {

      @Override
      public void run() {
        setBlockId(blockId);
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
