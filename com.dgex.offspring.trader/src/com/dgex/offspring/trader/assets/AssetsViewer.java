package com.dgex.offspring.trader.assets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nxt.Asset;
import nxt.util.Convert;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericComparator;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IFilteredStructuredContentProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.swt.table.Wildcard;

public class AssetsViewer extends GenerericTableViewer {

  static String EXTENT_COLUMN_NAME = "01234567890";
  static String EXTENT_COLUMN_ISSUER = "#############";
  static String EXTENT_COLUMN_QUANTITY = "1000000000";

  final IGenericTableColumn columnName = new GenericTableColumnBuilder("Name")
      .align(SWT.LEFT).textExtent(EXTENT_COLUMN_NAME)
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Asset asset = (Asset) element;
          return asset.getName();
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return ((String) v1).compareToIgnoreCase((String) v2);
          // return CompareMe.compare((String) v1, (String) v2);
        }
      }).build();

  final IGenericTableColumn columnIssuer = new GenericTableColumnBuilder(
      "Issuer").align(SWT.LEFT).textExtent(EXTENT_COLUMN_ISSUER)
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Asset asset = (Asset) element;
          Long id = asset.getAccountId();
          if (id != null) {
            // InspectAccountDialog.show(id, nxt, engine, userService, sync,
            // contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Asset asset = (Asset) element;
          return asset.getAccountId(); // Long
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

  final IGenericTableColumn columnQuantity = new GenericTableColumnBuilder(
      "Quantity").align(SWT.RIGHT).textExtent(EXTENT_COLUMN_QUANTITY)
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Asset asset = (Asset) element;
          return Long.valueOf(asset.getQuantityQNT());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Convert
              .toNXT((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IFilteredStructuredContentProvider contentProvider = new IFilteredStructuredContentProvider() {

    private AssetsViewer viewer = null;
    private String filter = null;

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.viewer = (AssetsViewer) viewer;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (viewer == null)
        return new Object[0];

      List<Asset> list = new ArrayList<Asset>(Asset.getAllAssets());
      Collections.sort(list, new Comparator<Asset>() {

        @Override
        public int compare(Asset s1, Asset s2) {
          return s1.getName().compareToIgnoreCase(s2.getName());
        }
      });

      if (filter != null) {
        List<Asset> filtered = new ArrayList<Asset>();
        for (Asset asset : list) {
          if (Wildcard.match(asset.getName().toLowerCase(), filter)) {
            filtered.add(asset);
          }
        }
        list = filtered;
      }
      return list.toArray(new Object[list.size()]);
    }

    @Override
    public void setFilter(String columnId, String filter) {
      if (filter != null)
        this.filter = filter.toLowerCase();
      else
        this.filter = null;
    }
  };

  public INxtService nxt;

  public AssetsViewer(Composite parent, INxtService nxt) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.nxt = nxt;
    setGenericTable(new IGenericTable() {

      @Override
      public int getDefaultSortDirection() {
        return GenericComparator.ASSCENDING;
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
        return new IGenericTableColumn[] { columnName, columnQuantity };
      }
    });
    refresh();
  }

}
