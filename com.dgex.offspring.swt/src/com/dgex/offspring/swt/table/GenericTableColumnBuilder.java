package com.dgex.offspring.swt.table;

import org.eclipse.swt.SWT;

public class GenericTableColumnBuilder {

  private String label;
  private int align = SWT.RIGHT;
  private boolean resizable = true;
  private String textExtent = "##########";
  private int width = -1;
  private boolean sortable = true;
  private ICellDataProvider provider = null;
  private boolean stretch = false;
  private boolean editable = true;
  private ICellActivateHandler activateHandler = null;

  public GenericTableColumnBuilder(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return "Column " + label + " " + width;
  }

  public IGenericTableColumn build() {
    return new GenericTableColumn(label, align, resizable, textExtent, width,
        sortable, stretch, provider, editable, activateHandler);
  }

  public GenericTableColumnBuilder provider(ICellDataProvider provider) {
    this.provider = provider;
    return this;
  }

  public GenericTableColumnBuilder activate(ICellActivateHandler activateHandler) {
    this.activateHandler = activateHandler;
    return this;
  }

  public GenericTableColumnBuilder editable(boolean editable) {
    this.editable = editable;
    return this;
  }

  public GenericTableColumnBuilder label(String label) {
    this.label = label;
    return this;
  }

  public GenericTableColumnBuilder align(int align) {
    this.align = align;
    return this;
  }

  public GenericTableColumnBuilder resizable(boolean resizable) {
    this.resizable = resizable;
    return this;
  }

  public GenericTableColumnBuilder textExtent(String textExtent) {
    this.textExtent = textExtent;
    return this;
  }

  public GenericTableColumnBuilder width(int width) {
    this.width = width;
    return this;
  }

  public GenericTableColumnBuilder sortable(boolean sortable) {
    this.sortable = sortable;
    return this;
  }

  public GenericTableColumnBuilder stretch(boolean stretch) {
    this.stretch = stretch;
    return this;
  }
}