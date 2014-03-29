package com.dgex.offspring.swt.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerEditor;
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
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

import com.dgex.offspring.config.Colors;

public class GenerericTreeViewer extends TreeViewer implements
    IGenericViewer {

  static final Logger logger = Logger.getLogger(GenerericTreeViewer.class);
  static final String FONT_KEY = "com.dgex.offspring.swt.table.GenerericTableViewer.FONT.";
  static final String COLOR_KEY = "com.dgex.offspring.swt.table.GenerericTableViewer.COLOR.";

  private IGenericTable table = null;
  private final GenericComparator comparator = null;

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
        getTree().setCursor(null);
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

          getTree().setCursor(
              cell.getControl().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        }
      }
    }
  };
  private Object lazyInput;

  public GenerericTreeViewer(Composite parent, int style) {
    super(parent, style | SWT.FULL_SELECTION);
  }

  @Override
  public void setGenericTable(IGenericTable table) {
    this.table = table;

    setUseHashlookup(true);
    setContentProvider(table.getContentProvider());
    // if (!(table.getContentProvider() instanceof
    // IPageableStructeredContentProvider)) {
    // this.comparator = new GenericComparator(table);
    // setComparator(comparator);
    // }

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

    TreeViewerEditor.create(this, actSupport,
        ColumnViewerEditor.TABBING_HORIZONTAL
            | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
            | ColumnViewerEditor.TABBING_VERTICAL
            | ColumnViewerEditor.KEYBOARD_ACTIVATION);

    createColumns();

    // setInput(null);
    // getTree().setHeaderVisible(true);
    getTree().setLinesVisible(true);
    getTree().layout();

    for (final IGenericTableColumn c : table.getColumns()) {
      if (c.getCellActivateHandler() != null) {
        getTree().addMouseMoveListener(mouseMoveListener);
        getTree().addMouseListener(mouseListener);
        break;
      }
    }
  }

  @Override
  public IGenericTable getGenericTable() {
    return this.table;
  }

  private void createColumns() {
    GC gc = new GC(getTree().getParent());

    List<Integer> widths = new ArrayList<Integer>();

    for (final IGenericTableColumn c : table.getColumns()) {
      TreeViewerColumn viewerColumn = new TreeViewerColumn(this, SWT.NONE);

      viewerColumn.setLabelProvider(new GenericLabelProvider(c
          .getDataProvider()));

      if (c.getEditable()) {
        viewerColumn.setEditingSupport(c.getEditingSupport(this));
      }

      TreeColumn column = viewerColumn.getColumn();

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

    // /* All columns have their prefered width set now calculate percentages */
    // TreeColumnLayout layout = new TreeColumnLayout();
    // for (int i = 0; i < widths.size(); i++) {
    // layout.setColumnData(getTree().getColumns()[i], new ColumnWeightData(
    // widths.get(i), widths.get(i), true));
    // }
    // getTree().getParent().setLayout(layout);
  }

  private SelectionAdapter getSelectionAdapter(final TreeColumn column,
      final IGenericTableColumn c) {
    SelectionAdapter selectionAdapter = new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        comparator.setColumn(c);
        int dir = comparator.getDirection();
        getTree().setSortDirection(dir);
        getTree().setSortColumn(column);
        refresh();
      }
    };
    return selectionAdapter;
  }
}
