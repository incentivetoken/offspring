package com.dgex.offspring.ui.controls;

import nxt.Alias;
import nxt.NxtException.ValidationException;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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

import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.core.AliasHelper;
import com.dgex.offspring.nxtCore.service.IAlias;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;
import com.dgex.offspring.swt.table.PaginationContainer;
import com.dgex.offspring.ui.PromptFeeDeadline;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class AliasControl extends Composite {

  private static Image errorImage = FieldDecorationRegistry.getDefault()
      .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

  private Text aliasNameText;
  private ControlDecoration decoAliasNameText;
  private Text aliasURIText;
  private Button aliasRegisterButton;
  private final AliasViewer aliasesViewer;
  private final Long accountId;
  private final INxtService nxt;
  private final IUser user;
  private final UISynchronize sync;
  private final IContactsService contactsService;

  private final PaginationContainer paginationContainer;

  public AliasControl(Composite parent, int style, Long accountId,
      INxtService nxt, IStylingEngine engine, IUserService userService,
      UISynchronize sync, IContactsService contactsService) {
    super(parent, style);
    this.accountId = accountId;
    this.nxt = nxt;
    this.user = userService.findUser(accountId);
    this.sync = sync;
    this.contactsService = contactsService;
    GridLayoutFactory.fillDefaults().spacing(10, 5).numColumns(3).applyTo(this);

    /* top bar (name, uri, register) */

    if (user != null && !user.getAccount().isReadOnly()) {
      aliasNameText = new Text(this, SWT.BORDER);
      GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
          .hint(100, SWT.DEFAULT).applyTo(aliasNameText);
      aliasNameText.setMessage("name");

      decoAliasNameText = new ControlDecoration(aliasNameText, SWT.TOP
          | SWT.RIGHT);
      decoAliasNameText.setImage(errorImage);
      decoAliasNameText.setDescriptionText("Alias is registered");
      decoAliasNameText.hide();

      aliasURIText = new Text(this, SWT.BORDER);
      GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
          .grab(true, false).applyTo(aliasURIText);
      aliasURIText.setMessage("uri");

      aliasRegisterButton = new Button(this, SWT.PUSH);
      GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
          .applyTo(aliasRegisterButton);

      aliasRegisterButton.setText("Register");
      aliasRegisterButton.setEnabled(false);

      hookupAliasControls();
    }

    /* bottom bar table viewer */

    paginationContainer = new PaginationContainer(this, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .span(3, 1).applyTo(paginationContainer);

    aliasesViewer = new AliasViewer(paginationContainer.getViewerParent(),
        accountId, nxt, engine, userService, sync, contactsService);
    paginationContainer.setTableViewer(aliasesViewer, 100);

    aliasesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {}
    });
  }

  public void refresh() {
    aliasesViewer.refresh();
  }

  private void hookupAliasControls() {
    aliasNameText.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        String name = aliasNameText.getText().trim();
        Alias alias = Alias.getAlias(name);

        if (alias == null) {
          decoAliasNameText.hide();
          aliasRegisterButton.setEnabled(true);
          aliasRegisterButton.setText("Register");
          aliasURIText.setText("");
        }
        else if (alias.getAccount().equals(user.getAccount().getNative())) {
          decoAliasNameText.hide();
          aliasRegisterButton.setEnabled(true);
          aliasRegisterButton.setText("Update");
        }
        else {
          decoAliasNameText.show();
          aliasRegisterButton.setEnabled(false);
          String value = alias.getURI();
          aliasURIText.setText(value.trim().isEmpty() ? ".. registered .."
              : value);
        }
      }
    });

    aliasRegisterButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        registerAlias();
      }
    });
  }

  private void registerAlias() {
    PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
    if (dialog.open() != Window.OK) {
      return;
    }

    final int fee = dialog.getFee();
    final short deadline = dialog.getDeadline();
    final String name = aliasNameText.getText().trim();
    final String uri = aliasURIText.getText().trim();

    BusyIndicator.showWhile(getDisplay(), new Runnable() {

      @Override
      public void run() {
        try {
          nxt.createAssignAliasTransaction(user.getAccount(), name, uri,
              deadline, fee, 0l);

          aliasRegisterButton.setEnabled(true);
          decoAliasNameText.show();

          IAlias alias = new AliasHelper(name, uri, user.getAccount()
              .getNative());
          nxt.getPendingAliases().add(alias);

          aliasesViewer.refresh();
        }
        catch (ValidationException e) {
          MessageDialog.openError(getShell(), "Validation Exception",
              e.toString());
        }
        catch (TransactionException e) {
          MessageDialog.openError(getShell(), "Transaction Exception",
              e.toString());
        }
      }
    });
  }

}
