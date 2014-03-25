package com.dgex.offspring.application.ui.home;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class SellOrderTableViewer extends TableViewer {

  private final SellOrderContentProvider contentProvider;

  private final SellOrderComparator comparator;

  public SellOrderTableViewer(Composite parent, UISynchronize sync) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
        | SWT.BORDER);

    this.contentProvider = new SellOrderContentProvider(sync);
    this.comparator = new SellOrderComparator();

    setUseHashlookup(false);
    setContentProvider(contentProvider);
    setComparator(comparator);

    createColumns();

    /* Pack the columns */
    for (TableColumn column : getTable().getColumns())
      column.pack();

    Table table = getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
  }

  private void createColumns() {
    for (int id : SellOrderTable.getColumns()) {
      TableViewerColumn viewerColumn = new TableViewerColumn(this, SWT.NONE);
      TableColumn column = viewerColumn.getColumn();

      viewerColumn.setEditingSupport(new SellOrderEditingSupport(this, id));

      viewerColumn.setLabelProvider(SellOrderTable.createLabelProvider(id));
      column.addSelectionListener(getSelectionAdapter(column, id));

      column.setText(SellOrderTable.getColumnLabel(id));
      column.setAlignment(SellOrderTable.getColumnAlignment(id));

      column.setResizable(SellOrderTable.getColumnResizable(id));
      column.setWidth(SellOrderTable.getColumnWidth(id));
    }
  }

  private SelectionAdapter getSelectionAdapter(final TableColumn column,
      final int index) {
    SelectionAdapter selectionAdapter = new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        comparator.setColumn(index);
        int dir = comparator.getDirection();
        getTable().setSortDirection(dir);
        getTable().setSortColumn(column);
        refresh();
      }
    };
    return selectionAdapter;
  }

}
