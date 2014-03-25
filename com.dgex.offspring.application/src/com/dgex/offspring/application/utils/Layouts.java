package com.dgex.offspring.application.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

public class Layouts {

  public static class Grid {

    public static GridLayout create(int columns) {
      return create(columns, 0, 0, 0, 0, 0, 0);
    }

    public static GridLayout create(int columns, int marginWidth, int marginHeight) {
      return create(columns, marginHeight, marginWidth, marginHeight, marginWidth, SWT.DEFAULT, SWT.DEFAULT);
    }

    public static GridLayout create(int columns, int marginWidth, int marginHeight, int horizontalSpacing, int verticalSpacing) {
      return create(columns, marginHeight, marginWidth, marginHeight, marginWidth, horizontalSpacing, verticalSpacing);
    }

    public static GridLayout create(int columns, int marginTop, int marginRight, int marginBottom, int marginLeft, int horizontalSpacing,
        int verticalSpacing) {
      GridLayout layout = new GridLayout(columns, false);
      if (marginBottom != SWT.DEFAULT)
        layout.marginBottom = marginBottom;
      if (marginTop != SWT.DEFAULT)
        layout.marginTop = marginTop;
      if (marginRight != SWT.DEFAULT)
        layout.marginRight = marginRight;
      if (marginLeft != SWT.DEFAULT)
        layout.marginLeft = marginLeft;
      if (horizontalSpacing != SWT.DEFAULT)
        layout.horizontalSpacing = horizontalSpacing;
      if (verticalSpacing != SWT.DEFAULT)
        layout.verticalSpacing = verticalSpacing;
      return layout;
    }

    public static GridData fill() {
      return fill(true, true);
    }

    public static GridData fill(boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace) {
      GridData gd = new GridData(GridData.FILL, GridData.FILL, grabExcessHorizontalSpace, grabExcessVerticalSpace);
      return gd;
    }

    public static GridData fill(int widthHint, int heightHint, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace) {
      GridData gd = fill(grabExcessHorizontalSpace, grabExcessVerticalSpace);
      gd.widthHint = widthHint;
      gd.heightHint = heightHint;
      return gd;
    }

  }
}
