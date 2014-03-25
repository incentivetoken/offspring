package com.dgex.offspring.ui.controls;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import nxt.Block;
import nxt.Constants;
import nxt.util.Convert;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.config.Formatter;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.core.BlockDB;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericComparator;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.ui.InspectAccountDialog;
import com.dgex.offspring.ui.InspectBlockDialog;
import com.dgex.offspring.user.service.IUserService;

public class GeneratedBlocksViewer extends GenerericTableViewer {

  final IGenericTableColumn columnHeight = new GenericTableColumnBuilder(
      "Height").align(SWT.RIGHT).textExtent("100000")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Integer.valueOf(block.getHeight());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Integer
              .toString((Integer) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnDate = new GenericTableColumnBuilder("Date")
      .align(SWT.LEFT).textExtent("dd MMM yy hh:mm:ss ")
      .provider(new ICellDataProvider() {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd MMM yy hh:mm:ss");

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          Date date = new Date(((block.getTimestamp()) * 1000l)
              + (Constants.EPOCH_BEGINNING - 500));
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

  final IGenericTableColumn columnId = new GenericTableColumnBuilder("Id")
      .align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Block block = (Block) element;
          Long id = block.getId();
          if (id != null) {
            InspectBlockDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Long.valueOf(block.getId());
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

  final IGenericTableColumn columnGenerator = new GenericTableColumnBuilder(
      "Generator").align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Block block = (Block) element;
          Long id = block.getGeneratorId();
          if (id != null) {
            InspectAccountDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Long.valueOf(block.getGeneratorId());
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

  final IGenericTableColumn columnAmount = new GenericTableColumnBuilder(
      "Amount").align(SWT.LEFT).textExtent("1000000000")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Integer.valueOf(block.getTotalAmount());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Integer
              .toString((Integer) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnFee = new GenericTableColumnBuilder("Fee")
      .align(SWT.LEFT).textExtent("1000000").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Integer.valueOf(block.getTotalFee());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Integer
              .toString((Integer) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnPayload = new GenericTableColumnBuilder(
      "Payload").align(SWT.RIGHT).textExtent("1000 KB")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Integer.valueOf(block.getPayloadLength());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Formatter
              .readableFileSize((Integer) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnBaseTarget = new GenericTableColumnBuilder(
      "BaseTarget").align(SWT.LEFT).textExtent("12345678901234567890")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Block block = (Block) element;
          return Long.valueOf(block.getBaseTarget());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Long
              .toString((Long) getCellValue(element));
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
      if (accountId == null)
        return new Object[0];

      List<Block> blocks = BlockDB.getGeneratedBlocks(accountId);
      return blocks.toArray(new Object[blocks.size()]);
    }
  };

  private IStylingEngine engine;
  private INxtService nxt;
  private IUserService userService;
  private UISynchronize sync;
  private IContactsService contactsService;

  public GeneratedBlocksViewer(Composite parent, Long accountId,
      IStylingEngine engine, INxtService nxt, IUserService userService,
      UISynchronize sync, IContactsService contactsService) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.engine = engine;
    this.nxt = nxt;
    this.userService = userService;
    this.sync = sync;
    this.contactsService = contactsService;
    setGenericTable(new IGenericTable() {

      @Override
      public int getDefaultSortDirection() {
        return GenericComparator.ASSCENDING;
      }

      @Override
      public IGenericTableColumn getDefaultSortColumn() {
        return columnDate;
      }

      @Override
      public IStructuredContentProvider getContentProvider() {
        return contentProvider;
      }

      @Override
      public IGenericTableColumn[] getColumns() {
        return new IGenericTableColumn[] { columnHeight, columnDate, columnId,
            columnGenerator, columnAmount, columnFee, columnPayload,
            columnBaseTarget };
      }
    });
    setInput(accountId);
  }
}
