package com.dgex.offspring.application.ui.blocks;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import com.dgex.offspring.nxtCore.service.INxtService;

public class BlockTableViewer extends TableViewer {

  private final BlockLazyContentProvider contentProvider;

  public BlockTableViewer(Composite parent, INxtService nxt) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
        | SWT.BORDER | SWT.VIRTUAL);

    contentProvider = new BlockLazyContentProvider(this);

    // TODO implement hash function on NXT model objects
    setUseHashlookup(true);
    setContentProvider(contentProvider);
    setInput(nxt);
    setItemCount(nxt.getBlockCount());

    createColumns();

    getTable().setHeaderVisible(true);
    getTable().setLinesVisible(true);
    refresh();
  }

  private void createColumns() {
    for (int id : BlockTable.getColumns()) {
      TableViewerColumn viewerColumn = new TableViewerColumn(this, SWT.NONE);
      TableColumn column = viewerColumn.getColumn();

      viewerColumn.setLabelProvider(BlockTable.createLabelProvider(id));

      column.setText(BlockTable.getColumnLabel(id));
      column.setAlignment(BlockTable.getColumnAlignment(id));

      column.setResizable(BlockTable.getColumnResizable(id));
      column.setWidth(BlockTable.getColumnWidth(id));
    }
  }
}
