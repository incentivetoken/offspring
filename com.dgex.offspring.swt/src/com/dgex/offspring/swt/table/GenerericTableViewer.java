package com.dgex.offspring.swt.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;

import com.dgex.offspring.config.Colors;

public class GenerericTableViewer extends TableViewer implements
    IGenericTableViewer {

  static final Logger logger = Logger.getLogger(GenerericTableViewer.class);
  static final String FONT_KEY = "com.dgex.offspring.swt.table.GenerericTableViewer.FONT.";
  static final String COLOR_KEY = "com.dgex.offspring.swt.table.GenerericTableViewer.COLOR.";

  private IGenericTable table = null;
  private GenericComparator comparator = null;

  private final MouseListener mouseListener = new MouseListener() {

    private boolean doubleClick;

    @Override
    public void mouseUp(MouseEvent e) {}

    @Override
    public void mouseDoubleClick(MouseEvent event) {
      doubleClick = true;
    }

    @Override
    public void mouseDown(final MouseEvent event) {
      doubleClick = false;
      Display.getDefault().timerExec(Display.getDefault().getDoubleClickTime(),
          new Runnable() {

            @Override
            public void run() {
              if (!doubleClick) {
                Point pt = new Point(event.x, event.y);
                ViewerCell cell = getCell(pt);
                if (cell != null) {
                  IGenericTableColumn column = getGenericTable().getColumns()[cell
                      .getColumnIndex()];
                  if (column.getCellActivateHandler() != null) {
                    column.getCellActivateHandler().activate(cell.getElement());
                  }
                }
              }
            }
          });
    }
  };

  /**
   * Mouse move listener responsible for highlighting cells. The current cell is
   * the ViewerCell currently highlighted. Cells are highlighted automatically
   * if their column returns a ICellActivateHandler.
   */
  private final MouseMoveListener mouseMoveListener = new MouseMoveListener() {

    private ViewerCell currentCell;

    @Override
    public void mouseMove(MouseEvent event) {
      Point pt = new Point(event.x, event.y);
      ViewerCell cell = getCell(pt);
      if (currentCell != null) {
        if (currentCell.equals(cell)) {
          return;
        }

        /* Must restore the previous cell */
        Widget widget = currentCell.getItem();
        if (widget != null && !widget.isDisposed()) {
          String font_key = FONT_KEY + currentCell.getColumnIndex();
          String color_key = COLOR_KEY + currentCell.getColumnIndex();

          currentCell.setForeground((Color) widget.getData(color_key));
          currentCell.setFont((Font) widget.getData(font_key));

          widget.setData(font_key, null);
          widget.setData(color_key, null);
        }
        getTable().setCursor(null);
      }

      if (cell != null) {
        IGenericTableColumn column = getGenericTable().getColumns()[cell
            .getColumnIndex()];
        if (column.getCellActivateHandler() != null) {

          /* Remember the original color and font */
          Widget widget = cell.getItem();
          if (widget != null && !widget.isDisposed()) {
            String font_key = FONT_KEY + cell.getColumnIndex();
            String color_key = COLOR_KEY + cell.getColumnIndex();

            widget.setData(font_key, cell.getFont());
            widget.setData(color_key, cell.getForeground());
          }

          /* Set the new currentCell and update the color and font */
          currentCell = cell;
          cell.setForeground(Colors.getColor(Colors.BLUE));
          cell.setFont(JFaceResources.getFontRegistry().getBold(""));

          getTable().setCursor(
              cell.getControl().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        }
      }
    }
  };

  public GenerericTableViewer(Composite parent, int style) {
    super(parent, style | SWT.FULL_SELECTION);
    // getTable().addListener(SWT.Resize, new Listener() {
    //
    // @Override
    // public void handleEvent(Event event) {
    // try {
    // getTable().setRedraw(false);
    // calculateSizes();
    // }
    // finally {
    // getTable().setRedraw(true);
    // }
    // getTable().layout();
    // }
    // });
  }

  @Override
  public void setGenericTable(IGenericTable table) {
    this.table = table;

    setUseHashlookup(true);
    setContentProvider(table.getContentProvider());
    if (!(table.getContentProvider() instanceof IPageableStructeredContentProvider)) {
      this.comparator = new GenericComparator(table);
      setComparator(comparator);
    }

    ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
        this) {

      @Override
      protected boolean isEditorActivationEvent(
          ColumnViewerEditorActivationEvent event) {
        return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
            || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
            || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
      }
    };

    TableViewerEditor.create(this, actSupport,
        ColumnViewerEditor.TABBING_HORIZONTAL
            | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
            | ColumnViewerEditor.TABBING_VERTICAL
            | ColumnViewerEditor.KEYBOARD_ACTIVATION);

    createColumns();
    // try {
    // getTable().setRedraw(false);
    // calculateSizes();
    // }
    // finally {
    // getTable().setRedraw(true);
    // }

    setInput(null);
    getTable().setHeaderVisible(true);
    getTable().setLinesVisible(true);
    getTable().layout();

    for (final IGenericTableColumn c : table.getColumns()) {
      if (c.getCellActivateHandler() != null) {
        getTable().addMouseMoveListener(mouseMoveListener);
        getTable().addMouseListener(mouseListener);
        break;
      }
    }
  }

  @Override
  public IGenericTable getGenericTable() {
    return this.table;
  }

  // @Override
  // public void autoResizeColumns() {
  // if (getTable() != null && !getTable().isDisposed()) {
  // calculateSizes();
  // }
  // }

  private void createColumns() {
    GC gc = new GC(getTable().getParent());

    List<Integer> widths = new ArrayList<Integer>();

    for (final IGenericTableColumn c : table.getColumns()) {
      TableViewerColumn viewerColumn = new TableViewerColumn(this, SWT.NONE);

      viewerColumn.setLabelProvider(new GenericLabelProvider(c
          .getDataProvider()));

      if (c.getEditable()) {
        viewerColumn.setEditingSupport(c.getEditingSupport(this));
      }

      TableColumn column = viewerColumn.getColumn();

      if (c.getSortable() && comparator != null) {
        column.addSelectionListener(getSelectionAdapter(column, c));
      }
      column.setText(c.getLabel());
      column.setAlignment(c.getAlignMent());

      int width;
      if (c.getWidth() != -1) {
        width = c.getWidth();
      }
      else if (c.getTextExtent() != null
          && c.getLabel().length() < c.getTextExtent().length()) {
        width = gc.textExtent(c.getTextExtent()).x + 2;
      }
      else {
        width = gc.textExtent(c.getLabel()).x + 2;
      }

      widths.add(width);
      column.setWidth(width);
      column.setResizable(c.getResizable());
    }
    gc.dispose();

    /* All columns have their prefered width set now calculate percentages */
    TableColumnLayout layout = new TableColumnLayout();
    for (int i = 0; i < widths.size(); i++) {
      layout.setColumnData(getTable().getColumns()[i], new ColumnWeightData(
          widths.get(i), widths.get(i), true));
    }
    getTable().getParent().setLayout(layout);
  }

  private SelectionAdapter getSelectionAdapter(final TableColumn column,
      final IGenericTableColumn c) {
    SelectionAdapter selectionAdapter = new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        comparator.setColumn(c);
        int dir = comparator.getDirection();
        getTable().setSortDirection(dir);
        getTable().setSortColumn(column);
        refresh();
      }
    };
    return selectionAdapter;
  }

  // /**
  // * Sizes are determined by explicit width, textExtent, label or stretch.
  // *
  // * If after resize the total column width becomes smaller than the client
  // area
  // * the remainder of the client area width is divided over all columns that
  // * have stretch set to yes.
  // *
  // * If after resize the total column width becomes larger than the client
  // area,
  // * all columns are first set to their default minimum widths, if the total
  // * width is now larger than the client area you add the remaining width to
  // all
  // * columns that have stretch. If the total width remains larger than the
  // * client area resize each column to equal width so that the total width is
  // * the same as the client area.
  // *
  // */
  // private void calculateSizes() {

  // GC gc = new GC(getTable().getParent());
  //
  // int columnCount = getTable().getColumnCount();
  // if (columnCount == 0)
  // return;
  //
  // /* Resize all columns to default size */
  // for (int i = 0; i < table.getColumns().length; i++) {
  // IGenericTableColumn c = table.getColumns()[i];
  // int width;
  // if (c.getWidth() != -1)
  // width = c.getWidth();
  // else if (c.getTextExtent() != null)
  // width = gc.textExtent(c.getTextExtent()).x + 2;
  // else
  // width = gc.textExtent(c.getLabel()).x + 2;
  //
  // TableColumn column = getTable().getColumns()[i];
  // column.setWidth(width);
  // }
  //
  // Rectangle area = getTable().getClientArea();
  // int totalAreaWdith = area.width;
  // int lineWidth = getTable().getGridLineWidth();
  // int totalGridLineWidth = (columnCount - 1) * lineWidth;
  // int totalColumnWidth = totalGridLineWidth;
  // for (TableColumn column : getTable().getColumns()) {
  // totalColumnWidth = totalColumnWidth + column.getWidth();
  // }
  //
  // /* width smaller then client area */
  // if (totalColumnWidth < totalAreaWdith) {
  // TableColumn column = null;
  //
  // /* stretch FIRST stretch column or last column if there is no STRETCH */
  // for (int i = 0; i < table.getColumns().length; i++) {
  // IGenericTableColumn c = table.getColumns()[i];
  // if (c.getStretch()) {
  // column = getTable().getColumns()[i];
  // break;
  // }
  // }
  // if (column == null) {
  // column = getTable().getColumns()[getTable().getColumns().length - 1];
  // }
  // column.setWidth(column.getWidth() + (totalAreaWdith - totalColumnWidth));
  // }
  //
  // /* width bigger than the client area */
  // else if (totalColumnWidth > totalAreaWdith) {
  //
  // int avgWidth = Math.round(totalAreaWdith / getTable().getColumnCount());
  // List<TableColumn> resizedColumns = new ArrayList<TableColumn>();
  //
  // int totalWidthNonResizedColumns = 0;
  // for (TableColumn column : getTable().getColumns()) {
  // if (column.getWidth() > avgWidth) {
  // resizedColumns.add(column);
  // column.setWidth(avgWidth);
  // }
  // else {
  // totalWidthNonResizedColumns += column.getWidth();
  // }
  // }
  //
  // /* calculate the total width again it could be less than available width
  // */
  // totalColumnWidth = totalGridLineWidth;
  // for (TableColumn column : getTable().getColumns()) {
  // totalColumnWidth = totalColumnWidth + column.getWidth();
  // }
  //
  // avgWidth = Math.round((totalAreaWdith - totalWidthNonResizedColumns)
  // / (resizedColumns.size()));
  // for (TableColumn column : resizedColumns) {
  // column.setWidth(avgWidth);
  // }
  // }
  //
  // gc.dispose();
  // }
}
