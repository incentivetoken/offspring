package com.dgex.offspring.trader.myassets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nxt.Account;
import nxt.Asset;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.Utils;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericComparator;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.ui.InspectAccountDialog;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class MyAssetsViewer extends GenerericTableViewer {

  static String EXTENT_COLUMN_NAME = "01234567890";
  static String EXTENT_COLUMN_BALANCE = "1000000000";
  static String EXTENT_COLUMN_ISSUER = "#############";
  static String EXTENT_COLUMN_QUANTITY = "1000000000";
  static String EXTENT_COLUMN_DESCRIPTION = "###############";
  static final String EMPTY_STRING = "";
  static Logger logger = Logger.getLogger(MyAssetsViewer.class);

  final IGenericTableColumn columnName = new GenericTableColumnBuilder("Name")
      .align(SWT.LEFT).textExtent(EXTENT_COLUMN_NAME)
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null)
            return asset.getName();
          return EMPTY_STRING;
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((String) v1, (String) v2);
        }
      }).build();

  final IGenericTableColumn columnBalance = new GenericTableColumnBuilder(
      "Balance").align(SWT.RIGHT).textExtent(EXTENT_COLUMN_BALANCE)
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          if (account.getAssetBalancesQNT() != null)
            return account.getAssetBalancesQNT().get(id);
          return Long.valueOf(0l);
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          data[ICellDataProvider.TEXT] = Utils
              .quantToString((Long) getCellValue(element), asset.getDecimals());
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnQuantity = new GenericTableColumnBuilder(
      "Quantity").align(SWT.RIGHT).textExtent(EXTENT_COLUMN_QUANTITY)
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null)
            return Long.valueOf(asset.getQuantityQNT());
          return Long.valueOf(0);
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);          
          data[ICellDataProvider.TEXT] = Utils
              .quantToString((Long) getCellValue(element), asset.getDecimals());
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnIssuer = new GenericTableColumnBuilder(
      "Issuer").align(SWT.LEFT).textExtent(EXTENT_COLUMN_ISSUER)
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null) {
            Long accountId = asset.getAccountId();
            if (accountId != null) {
              InspectAccountDialog.show(accountId, nxt, engine, userService,
                  sync, contactsService);
            }
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null)
            return asset.getAccountId();
          return Long.valueOf(0l);
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Convert
              .toUnsignedLong((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnDescription = new GenericTableColumnBuilder(
      "Description").align(SWT.LEFT).textExtent(EXTENT_COLUMN_DESCRIPTION)
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Long id = (Long) element;
          Asset asset = Asset.getAsset(id);
          if (asset != null)
            return asset.getDescription();
          return EMPTY_STRING;
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((String) v1, (String) v2);
        }
      }).build();

  final IStructuredContentProvider contentProvider = new IStructuredContentProvider() {

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

    @Override
    public Object[] getElements(Object inputElement) {
      IUser user = userService.getActiveUser();
      if (user == null) {
        return new Object[0];
      }

      account = user.getAccount().getNative();
      if (account == null) {
        return new Object[0];
      }

      Map<Long, Long> balances = account.getAssetBalancesQNT();
      if (balances == null) {
        return new Object[0];
      }

      List<Long> elements = new ArrayList<Long>(balances.keySet());
      return elements.toArray(new Object[elements.size()]);
    }
  };

  public INxtService nxt;
  private IUserService userService;
  private IContactsService contactsService;
  private UISynchronize sync;
  private Account account = null;
  private IStylingEngine engine;

  public MyAssetsViewer(Composite parent, INxtService nxt,
      IUserService userService, IContactsService contactsService,
      UISynchronize sync, IStylingEngine engine) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.nxt = nxt;
    this.userService = userService;
    this.contactsService = contactsService;
    this.sync = sync;
    this.engine = engine;
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
        return new IGenericTableColumn[] { columnName, columnBalance,
            columnQuantity, columnIssuer, columnDescription };
      }
    });
    setInput(1);
  }
}
