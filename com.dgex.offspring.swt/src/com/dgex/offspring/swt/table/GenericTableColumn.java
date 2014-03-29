package com.dgex.offspring.swt.table;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

public class GenericTableColumn implements IGenericTableColumn {

  private final String label;
  private final int align;
  private final boolean resizable;
  private final String textExtent;
  private final int width;
  private final boolean sortable;
  private final boolean stretch;
  private final ICellDataProvider labelProvider;
  private final boolean editable;
  private final ICellActivateHandler activateHandler;

  public GenericTableColumn(String label, int align, boolean resizable,
      String textExtent, int width, boolean sortable, boolean stretch,
      ICellDataProvider labelProvider, boolean editable,
      ICellActivateHandler activateHandler) {
    this.label = label;
    this.align = align;
    this.resizable = resizable;
    this.textExtent = textExtent;
    this.width = width;
    this.sortable = sortable;
    this.stretch = stretch;
    this.labelProvider = labelProvider;
    this.editable = editable;
    this.activateHandler = activateHandler;
  }

  @Override
  public EditingSupport getEditingSupport(ColumnViewer viewer) {
    return new GenericEditingSupport(viewer, this);
  }

  @Override
  public ICellActivateHandler getCellActivateHandler() {
    return activateHandler;
  }

  @Override
  public boolean getEditable() {
    return editable;
  }

  @Override
  public ICellDataProvider getDataProvider() {
    return labelProvider;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public int getAlignMent() {
    return align;
  }

  @Override
  public boolean getResizable() {
    return resizable;
  }

  @Override
  public String getTextExtent() {
    return textExtent;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public boolean getSortable() {
    return sortable;
  }

  @Override
  public boolean getStretch() {
    return stretch;
  }

};