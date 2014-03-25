package com.dgex.offspring.application.ui.messages;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;

import com.dgex.offspring.application.utils.ICellDataLabelProvider;
import com.dgex.offspring.nxtCore.service.IMessage;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.user.service.IUser;

public class MessagesEditingSupport extends EditingSupport {

  private final CellEditor editor;

  private final int columnId;

  private final ICellDataLabelProvider provider;

  public MessagesEditingSupport(MessagesTableViewer viewer, int columnId,
      IUser user, INxtService nxt) {
    super(viewer);
    this.editor = new TextCellEditor(viewer.getTable());
    this.provider = new MessagesLabelProvider(viewer, user, nxt);
    this.columnId = columnId;
  }

  @Override
  protected CellEditor getCellEditor(Object element) {
    return editor;
  }

  @Override
  protected boolean canEdit(Object element) {
    return true;
  }

  @Override
  protected Object getValue(Object element) {
    IMessage t = (IMessage) element;
    Object[] data = { null, null, null, null };
    this.provider.getCellData(t, columnId, data);
    return data[ICellDataLabelProvider.TEXT];
  }

  @Override
  protected void setValue(Object element, Object value) {}
}