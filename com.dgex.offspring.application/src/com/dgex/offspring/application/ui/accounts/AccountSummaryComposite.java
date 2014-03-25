package com.dgex.offspring.application.ui.accounts;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.dgex.offspring.messages.Messages;
import com.dgex.offspring.user.service.IUser;

public class AccountSummaryComposite extends Composite {

  static Logger logger = Logger.getLogger(AccountSummaryComposite.class);

  private final Label labelTransactions;
  private final Label labelForgedBlocks;
  private final Label labelForgedFee;
  private final Label labelMessages;
  private final Label labelAliases;
  private final Label labelAssets;

  private final Label labelAmount;
  private final Label labelLastTransfer;
  private final Label labelFirstTransfer;

  private final int avgCharCountTransactions = 10;
  private final int avgCharCountForgedBlocks = 10;
  private final int avgCharCountForgedFee = 10;
  private final int avgCharCountMessages = 10;
  private final int avgCharCountAliases = 10;
  private final int avgCharCountAssets = 10;

  public AccountSummaryComposite(Composite parent, int style) {
    super(parent, style);

    GridLayoutFactory.fillDefaults().numColumns(3).applyTo(this);

    Composite leftComposite = new Composite(this, SWT.NONE);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(leftComposite);
    GridLayoutFactory.fillDefaults().spacing(LayoutConstants.getSpacing())
        .numColumns(2).applyTo(leftComposite);

    Composite middleComposite = new Composite(this, SWT.NONE);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(middleComposite);
    GridLayoutFactory.fillDefaults().spacing(LayoutConstants.getSpacing())
        .numColumns(2).applyTo(middleComposite);

    Composite rightComposite = new Composite(this, SWT.NONE);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(rightComposite);
    GridLayoutFactory.fillDefaults().spacing(LayoutConstants.getSpacing())
        .numColumns(2).applyTo(rightComposite);

    /* == left == */

    Label label = new Label(leftComposite, SWT.NONE);
    label.setText(Messages.AccountSummaryComposite_label_transactions);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(label);

    labelTransactions = new Label(leftComposite, SWT.NONE);
    labelTransactions
        .setText(Messages.AccountSummaryComposite_transactions_default_value);

    label = new Label(leftComposite, SWT.NONE);
    label.setText(Messages.AccountSummaryComposite_label_forged_blocks);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(label);

    labelForgedBlocks = new Label(leftComposite, SWT.NONE);
    labelForgedBlocks
        .setText(Messages.AccountSummaryComposite_forged_blocks_default_value);

    label = new Label(leftComposite, SWT.NONE);
    label.setText(Messages.AccountSummaryComposite_label_forged_fee);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(label);

    labelForgedFee = new Label(leftComposite, SWT.NONE);
    labelForgedFee
        .setText(Messages.AccountSummaryComposite_forged_fee_default_value);

    /* == middle == */

    label = new Label(middleComposite, SWT.NONE);
    label.setText(Messages.AccountSummaryComposite_label_message_count);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(label);

    labelMessages = new Label(middleComposite, SWT.NONE);
    labelMessages
        .setText(Messages.AccountSummaryComposite_message_count_default_value);

    label = new Label(middleComposite, SWT.NONE);
    label.setText(Messages.AccountSummaryComposite_label_alias_count);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(label);

    labelAliases = new Label(middleComposite, SWT.NONE);
    labelAliases
        .setText(Messages.AccountSummaryComposite_alias_count_default_value);

    label = new Label(middleComposite, SWT.NONE);
    label.setText(Messages.AccountSummaryComposite_label_assets_count);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(label);

    labelAssets = new Label(middleComposite, SWT.NONE);
    labelAssets
        .setText(Messages.AccountSummaryComposite_assets_count_default_value);

    /* == right == */

    label = new Label(rightComposite, SWT.NONE);
    label.setText(Messages.AccountSummaryComposite_label_last_transfer);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(label);

    labelLastTransfer = new Label(rightComposite, SWT.NONE);
    labelLastTransfer.setText("---------------------"); //$NON-NLS-1$

    label = new Label(rightComposite, SWT.NONE);
    label.setText(Messages.AccountSummaryComposite_label_first_transafer);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(label);

    labelFirstTransfer = new Label(rightComposite, SWT.NONE);
    labelFirstTransfer.setText("---------------------"); //$NON-NLS-1$

    label = new Label(rightComposite, SWT.NONE);
    label.setText(Messages.AccountSummaryComposite_label_amount_inout);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(label);

    labelAmount = new Label(rightComposite, SWT.NONE);
    labelAmount.setText("---------------------"); //$NON-NLS-1$

    /*
     * Calculate sane minimum widths for labels (for styling individual labels)
     */

    int[] avgCharWitdh = { avgCharCountTransactions, avgCharCountForgedBlocks,
        avgCharCountForgedFee, avgCharCountMessages, avgCharCountAliases,
        avgCharCountAssets, };
    Label[] labels = { labelTransactions, labelForgedBlocks, labelForgedFee,
        labelMessages, labelAliases, labelAssets };
    if (avgCharWitdh.length != labels.length)
      throw new RuntimeException("Internal Error. Please look at me."); //$NON-NLS-1$

    for (int i = 0; i < labels.length; i++) {
      GC gc = new GC(labels[i]);
      FontMetrics fm = gc.getFontMetrics();
      int charWidth = fm.getAverageCharWidth();
      gc.dispose();
      GridDataFactory.defaultsFor(labels[i])
          .hint((charWidth * avgCharWitdh[i]), SWT.DEFAULT).grab(false, true)
          /* .indent(LayoutConstants.getMargins().x, 0) */.applyTo(labels[i]);
    }
  }

  public void setActiveUser(IUser user) {

    logger.info("setActiveUser DISABLED");

    // List<ITransaction> transactions = user.getAccount().getTransactions();
    //
    //    labelTransactions.setText("" + transactions.size()); //$NON-NLS-1$
    //
    //    String text = ""; //$NON-NLS-1$
    // if (transactions.size() > 0)
    // text = Formatter.formatTimestampLocale(transactions.get(0).getTimestamp()
    // .getTime());
    // labelFirstTransfer.setText(text);
    //
    //    text = ""; //$NON-NLS-1$
    // if (transactions.size() > 0)
    // text = Formatter.formatTimestampLocale(transactions
    // .get(transactions.size() - 1).getTimestamp().getTime());
    // labelLastTransfer.setText(text);
    //
    // IAccount account = user.getAccount();
    //
    // long forgedBlocks = account.getForgedBlocks().size();
    // long forgedFee = account.getForgedFee();
    // long amountIn = 0l;
    // long amountOut = 0l;
    // long feePaid = 0l;
    //
    // /* Count totals */
    // for (ITransaction t : transactions) {
    // amountIn += t.getAmountReceived(account.getId());
    // amountOut += t.getAmountSpend(account.getId());
    // feePaid += t.getFeePaid(account.getId());
    // }
    //
    // labelForgedBlocks.setText(Long.toString(forgedBlocks));
    // labelForgedFee.setText(Long.toString(forgedFee));
    //    labelAmount.setText(Long.toString(amountIn) + "/" //$NON-NLS-1$
    // + Long.toString(amountOut) + "/" + Long.toString(feePaid));
  }
}
