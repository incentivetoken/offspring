package com.dgex.offspring.application.ui.peers;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import com.dgex.offspring.nxtCore.service.INxtService;

public class PeerTableViewer extends TableViewer {

  private final int peerType;

  private final PeerContentProvider contentProvider;

  private final PeerComparator comparator = new PeerComparator();

  public PeerTableViewer(Composite parent, INxtService nxt, int peerType) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
        | SWT.BORDER);

    this.peerType = peerType;

    contentProvider = new PeerContentProvider(peerType);

    // TODO implement hash function on NXT model objects
    setUseHashlookup(false);
    setContentProvider(contentProvider);
    setInput(nxt);
    setComparator(comparator);

    createColumns();

    getTable().setHeaderVisible(true);
    getTable().setLinesVisible(true);
    refresh();
  }

  private void createColumns() {
    for (int id : PeerTable.getColumns(peerType)) {
      TableViewerColumn viewerColumn = new TableViewerColumn(this, SWT.NONE);
      TableColumn column = viewerColumn.getColumn();

      viewerColumn
          .setLabelProvider(PeerTable.createLabelProvider(id, peerType));
      column.addSelectionListener(getSelectionAdapter(column, id));

      column.setText(PeerTable.getColumnLabel(id));
      column.setAlignment(PeerTable.getColumnAlignment(id));

      column.setResizable(PeerTable.getColumnResizable(id));
      column.setWidth(PeerTable.getColumnWidth(id));
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
