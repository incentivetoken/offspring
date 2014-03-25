package com.dgex.offspring.application.ui.messages;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.dgex.offspring.application.ui.messages.MessagesComparator;
import com.dgex.offspring.application.ui.messages.MessagesContentProvider;
import com.dgex.offspring.application.ui.messages.MessagesEditingSupport;
import com.dgex.offspring.application.ui.messages.MessagesLabelProvider;
import com.dgex.offspring.application.ui.messages.MessagesTable;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.user.service.IUser;

public class MessagesTableViewer extends TableViewer {

  private final MessagesContentProvider contentProvider;
  private final MessagesComparator comparator;
  private final IUser user;
  private final INxtService nxt;

  public MessagesTableViewer(Composite parent, IUser user, INxtService nxt) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
        | SWT.BORDER);

    this.user = user;
    this.nxt = nxt;

    contentProvider = new MessagesContentProvider();
    comparator = new MessagesComparator(user);

    setUseHashlookup(true);
    setContentProvider(contentProvider);
    setInput(user);
    setComparator(comparator);

    createColumns();

    Table table = getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
  }

  public IUser getUser() {
    return user;
  }

  private void createColumns() {
    for (int id : MessagesTable.getColumns()) {
      TableViewerColumn viewerColumn = new TableViewerColumn(this, SWT.NONE);
      TableColumn column = viewerColumn.getColumn();

      viewerColumn.setEditingSupport(new MessagesEditingSupport(this, id, user,
          nxt));

      viewerColumn.setLabelProvider(new MessagesLabelProvider(this, user, nxt));
      column.addSelectionListener(getSelectionAdapter(column, id));

      column.setText(MessagesTable.getColumnLabel(id));
      column.setAlignment(MessagesTable.getColumnAlignment(id));

      column.setResizable(MessagesTable.getColumnResizable(id));
      column.setWidth(MessagesTable.getColumnWidth(id));
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
