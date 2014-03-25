package com.dgex.offspring.config;

public class Paginator {

  private int pageSize;
  private int page;
  private int startingIndex;
  private int endingIndex;
  private int maxPages;
  private final int count;

  public Paginator(int page, int pageSize, int count) {
    this.page = page;
    this.pageSize = pageSize;
    this.maxPages = 1;
    this.count = 1;
    calculatePages();
    setPage(page);
  }

  private void calculatePages() {
    if (pageSize > 0) {
      if (count % pageSize == 0) {
        maxPages = count / pageSize;
      }
      else {
        maxPages = (count / pageSize) + 1;
      }
    }
  }

  public int getStartingIndex() {
    return startingIndex;
  }

  public int getEndIndex() {
    return endingIndex;
  }

  public int getPageSize() {
    return this.pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
    calculatePages();
  }

  public int getPage() {
    return this.page;
  }

  public void setPage(int p) {
    if (p >= maxPages) {
      this.page = maxPages;
    }
    else if (p <= 1) {
      this.page = 1;
    }
    else {
      this.page = p;
    }

    // now work out where the sub-list should start and end
    startingIndex = pageSize * (page - 1);
    if (startingIndex < 0) {
      startingIndex = 0;
    }
    endingIndex = startingIndex + pageSize;
    if (endingIndex > count) {
      endingIndex = count;
    }
  }

  public int getMaxPages() {
    return this.maxPages;
  }

  public int getPreviousPage() {
    if (page > 1) {
      return page - 1;
    }
    else {
      return 0;
    }
  }

  public int getNextPage() {
    if (page < maxPages) {
      return page + 1;
    }
    else {
      return 0;
    }
  }

}