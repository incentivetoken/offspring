package com.dgex.offspring.application.ui.accounts;

import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.dgex.offspring.application.utils.Layouts;
import com.dgex.offspring.config.CSSClasses;
import com.dgex.offspring.messages.Messages;

public class AccountTotalsComposite extends Composite {

  private final Label totalLabel;

  private long totalValue = 0l;

  public AccountTotalsComposite(final Composite parent, int style,
      IStylingEngine engine) {
    super(parent, style);
    setLayout(Layouts.Grid.create(2, 0, 2, 5, 0, 0, 0));
    engine.setClassname(this, CSSClasses.ACCOUNT_TOTALS);

    Label label = new Label(this, SWT.NONE);
    label.setText(Messages.AccountTotalsComposite_label_total);
    label
        .setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, true));

    totalLabel = new Label(this, SWT.NONE);
    totalLabel.setLayoutData(new GridData(GridData.END, GridData.FILL, true,
        true));
    engine.setClassname(totalLabel, CSSClasses.TOTAL);

    Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
    totalLabel.setMenu(menu);

    MenuItem item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.AccountTotalsComposite_label_copy_total);
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Clipboard clipboard = new Clipboard(parent.getDisplay());
        TextTransfer textTransfer = TextTransfer.getInstance();
        clipboard.setContents(new String[] { Long.toString(totalValue) },
            new Transfer[] { textTransfer });
        clipboard.dispose();
      }
    });

    /*
     * To achieve the fixed width based on the left column we set an invisible
     * account label here
     */
    GridData gd = new GridData();
    gd.horizontalSpan = 2;
    label = new Label(this, SWT.NONE);
    label.setLayoutData(gd);
    label.setText("0000000000000000000000"); //$NON-NLS-1$
    label.setVisible(false);
    engine.setClassname(label, CSSClasses.ACCOUNT_NUMBER);

    setTotal(0l);
  }

  public void setTotal(Long total) {
    totalValue = total;
    if (totalLabel != null && !totalLabel.isDisposed()) {
      totalLabel.setText(Long.toString(total) + " NXT"); //$NON-NLS-1$
      pack();
      layout();
    }
  }
}