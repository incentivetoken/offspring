package com.dgex.offspring.ui.controls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nxt.Account;
import nxt.Alias;
import nxt.Constants;
import nxt.util.Convert;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.messages.Messages;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericComparator;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.ui.InspectTransactionDialog;
import com.dgex.offspring.user.service.IUserService;

public class AliasViewer extends GenerericTableViewer {

  final IGenericTableColumn columnName = new GenericTableColumnBuilder("Name")
      .align(SWT.LEFT).textExtent("##########")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Alias alias = (Alias) element;
          return alias.getAliasName();
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((String) v1, (String) v2);
        }
      }).build();

  final IGenericTableColumn columnURI = new GenericTableColumnBuilder("URI")
      .align(SWT.LEFT).textExtent("##############################")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Alias alias = (Alias) element;
          return alias.getURI();
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((String) v1, (String) v2);
        }
      }).build();

  final IGenericTableColumn columnDate = new GenericTableColumnBuilder(
      Messages.TransactionTable_column_date_label).align(SWT.LEFT)
      .textExtent("dd MMM yy hh:mm:ss ").provider(new ICellDataProvider() {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd MMM yy hh:mm:ss");

        @Override
        public Object getCellValue(Object element) {
          Alias alias = (Alias) element;
          Date date = new Date(((alias.getTimestamp()) * 1000L)
              + (Constants.EPOCH_BEGINNING - 500L));
          return date;
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = dateFormat.format(getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare(((Date) v1).getTime(), ((Date) v2).getTime());
        }
      }).build();

  final IGenericTableColumn columnID = new GenericTableColumnBuilder(
      Messages.TransactionTable_column_transaction_label).align(SWT.LEFT)
      .textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Alias alias = (Alias) element;
          Long id = alias.getId();
          if (id != null) {
            InspectTransactionDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Alias alias = (Alias) element;
          return Long.valueOf(alias.getId());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = Convert.toUnsignedLong((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IStructuredContentProvider contentProvider = new IStructuredContentProvider() {

    private Long accountId = null;

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.accountId = (Long) newInput;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (accountId == null) {
        return new Object[0];
      }

      Account account = Account.getAccount(accountId);
      if (account == null) {
        return new Object[0];
      }

      List<Alias> elements = new ArrayList<Alias>();
      for (Alias alias : Alias.getAllAliases()) {
        if (alias.getAccount().equals(account)) {
          elements.add(alias);
        }
      }

      return elements.toArray(new Object[elements.size()]);
    }
  };

  private INxtService nxt;
  private IStylingEngine engine;
  private IUserService userService;
  private UISynchronize sync;
  private IContactsService contactsService;

  public AliasViewer(Composite parent, Long accountId, INxtService nxt,
      IStylingEngine engine, IUserService userService, UISynchronize sync,
      IContactsService contactsService) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.nxt = nxt;
    this.engine = engine;
    this.userService = userService;
    this.sync = sync;
    this.contactsService = contactsService;
    setGenericTable(new IGenericTable() {

      @Override
      public int getDefaultSortDirection() {
        return GenericComparator.DESCENDING;
      }

      @Override
      public IGenericTableColumn getDefaultSortColumn() {
        return columnName;
      }

      @Override
      public IStructuredContentProvider getContentProvider() {
        return contentProvider;
      }

      @Override
      public IGenericTableColumn[] getColumns() {
        return new IGenericTableColumn[] { columnName, columnURI, columnID,
            columnDate };
      }
    });
    setInput(accountId);
  }
}
