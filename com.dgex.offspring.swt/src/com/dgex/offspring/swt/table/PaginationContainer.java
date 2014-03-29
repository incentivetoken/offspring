package com.dgex.offspring.swt.table;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

public class PaginationContainer extends Composite {

  static Logger logger = Logger.getLogger(PaginationContainer.class);
  private final Label pageLabel;
  private PaginatedContentProvider contentProvider;
  private ColumnViewer viewer;
  private final Link previousLink;
  private final Link nextLink;
  private final Composite viewerComposite;

  static class PaginatedContentProvider implements IStructuredContentProvider {

    private final IStructuredContentProvider contentProvider;
    private final PaginationContainer container;
    private final int pageSize;
    private Pageable<Object> pageable = null;
    private int currentPage = 1;
    private int pageCount = 1;
    private int nextPage = 0;
    private int previousPage = 0;

    public PaginatedContentProvider(PaginationContainer container,
        IStructuredContentProvider contentProvider, int pageSize) {
      this.container = container;
      this.contentProvider = contentProvider;
      this.pageSize = pageSize;
    }

    @Override
    public void dispose() {
      contentProvider.dispose();
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.contentProvider.inputChanged(viewer, oldInput, newInput);
    }

    private int calculatePages(int count) {
      if (pageSize > 0) {
        if (count % pageSize == 0)
          return count / pageSize;
        else
          return (count / pageSize) + 1;
      }
      return 0;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (contentProvider instanceof IPageableStructeredContentProvider) {
        IPageableStructeredContentProvider cp = (IPageableStructeredContentProvider) contentProvider;

        this.pageCount = calculatePages(cp.getElementCount());
        this.currentPage = currentPage <= 1 ? 1 : currentPage;

        // cp.setPageSize(pageSize);
        cp.setCurrentPage(Math.min(currentPage, pageCount));

        this.nextPage = currentPage < pageCount ? currentPage + 1 : 0;
        this.previousPage = currentPage > 1 ? currentPage - 1 : 0;

        this.container.updateButtons();

        logger.info("getElements() START");
        Object[] elements = cp.getElements(inputElement);
        logger.info("getElements() END returns=" + elements.length);
        return elements;
      }
      else {
        Object[] elements = this.contentProvider.getElements(inputElement);
        this.pageable = new Pageable<Object>(Arrays.asList(elements), pageSize);
        this.pageable
            .setPage(Math.min(currentPage, this.pageable.getMaxPages()));

        this.pageCount = pageable.getMaxPages();
        this.currentPage = pageable.getPage();
        this.nextPage = pageable.getNextPage();
        this.previousPage = pageable.getPreviousPage();

        this.container.updateButtons();

        List<Object> pagedList = this.pageable.getListForPage();
        return pagedList.toArray(new Object[pagedList.size()]);
      }
    }

    public int getPageCount() {
      return pageCount;
    }

    public int getCurrentPage() {
      return currentPage;
    }

    public int nextPage() {
      return nextPage;
    }

    public int previousPage() {
      return previousPage;
    }

    public void setPage(int currentPage) {
      this.currentPage = Math.min(currentPage, getPageCount());
    }
  };

  public PaginationContainer(Composite parent, int style) {
    super(parent, style);

    GridLayoutFactory.fillDefaults().numColumns(3).margins(2, 0).spacing(2, 0)
        .applyTo(this);

    previousLink = new Link(this, SWT.NONE);
    previousLink.setVisible(false);
    GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
        .exclude(true).applyTo(previousLink);
    previousLink.setText("<A>previous</A>");
    previousLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        final int previousPage = contentProvider.previousPage();
        if (previousPage != contentProvider.getCurrentPage()) {
          BusyIndicator.showWhile(getDisplay(), new Runnable() {

            @Override
            public void run() {
              contentProvider.setPage(previousPage);
              viewer.refresh();
            }
          });
        }
      }
    });

    nextLink = new Link(this, SWT.NONE);
    nextLink.setVisible(false);
    GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
        .exclude(true).applyTo(nextLink);
    nextLink.setText("<A>next</A>");
    nextLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        final int nextPage = contentProvider.nextPage();
        if (nextPage != contentProvider.getCurrentPage()) {
          BusyIndicator.showWhile(getDisplay(), new Runnable() {

            @Override
            public void run() {
              contentProvider.setPage(nextPage);
              viewer.refresh();
            }
          });
        }
      }
    });

    pageLabel = new Label(this, SWT.NONE);
    pageLabel.setVisible(false);
    GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false)
        .exclude(true).applyTo(pageLabel);

    viewerComposite = new Composite(this, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .span(3, 1).applyTo(viewerComposite);
  }

  public Composite getViewerParent() {
    return viewerComposite;
  }

  private void updateButtons() {
    int pageCount = this.contentProvider.getPageCount();
    if (pageCount > 1) {
      setButtonsExcluded(false);
      int currentPage = this.contentProvider.getCurrentPage();
      previousLink.setEnabled(currentPage > 1);
      nextLink.setEnabled(currentPage < pageCount);

      pageLabel.setText(currentPage + "/" + pageCount);
      pageLabel.pack();
    }
    else {
      setButtonsExcluded(true);
    }
    layout();
  }

  private void setButtonsExcluded(boolean exclude) {
    ((GridData) previousLink.getLayoutData()).exclude = exclude;
    ((GridData) nextLink.getLayoutData()).exclude = exclude;
    ((GridData) pageLabel.getLayoutData()).exclude = exclude;
    previousLink.setVisible(!exclude);
    nextLink.setVisible(!exclude);
    pageLabel.setVisible(!exclude);
  }

  /**
   * The TableViewer is already constructed and uses this composite as it's
   * parent. This works because in the constructor we create all other children
   * of this composite so they will be rendered before the table viewer. You
   * dont need to set any layout data on the table viewer, this method will take
   * care of that.
   * 
   * @param viewer
   */
  public void setTableViewer(ColumnViewer viewer, int pageSize) {
    this.viewer = viewer;
    IStructuredContentProvider inner = ((IGenericViewer) viewer)
        .getGenericTable().getContentProvider();

    if (inner instanceof IPageableStructeredContentProvider) {
      ((IPageableStructeredContentProvider) inner).setPageSize(pageSize);
    }

    contentProvider = new PaginatedContentProvider(this, inner, pageSize);
    viewer.setContentProvider(contentProvider);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .span(5, 1).applyTo(viewer.getControl());
  }
}
