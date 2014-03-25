package com.dgex.offspring.application.ui.messages;

import org.eclipse.swt.SWT;

public class MessagesTable {

  public final static int COLUMN_TYPE = 31;
  public final static int COLUMN_TIMESTAMP = 32;
  public final static int COLUMN_ID = 33;
  public final static int COLUMN_RECIPIENT = 34;
  public final static int COLUMN_MESSAGE = 35;
  public static final int COLUMN_SENDER = 36;

  private final static int[] columns = new int[] { COLUMN_TYPE, COLUMN_MESSAGE,
      COLUMN_TIMESTAMP, COLUMN_ID };

  public static int[] getColumns() {
    return columns;
  }

  public static String getColumnLabel(int id) {
    switch (id) {
    case COLUMN_TYPE:
      return "#";
    case COLUMN_TIMESTAMP:
      return "Date";
    case COLUMN_ID:
      return "ID";
    case COLUMN_RECIPIENT:
      return "Recipient";
    case COLUMN_SENDER:
      return "Sender";
    case COLUMN_MESSAGE:
      return "Message";
    }
    return "FAILURE"; //$NON-NLS-1$
  }

  public static int getColumnWidth(int id) {
    switch (id) {
    case COLUMN_TYPE:
      return 40;
    case COLUMN_TIMESTAMP:
    case COLUMN_SENDER:
    case COLUMN_ID:
    case COLUMN_RECIPIENT:
    case COLUMN_MESSAGE:
      return 200;
    }
    return 200;
  }

  public static int getColumnAlignment(int id) {
    return SWT.LEFT;
  }

  public static boolean getColumnResizable(int id) {
    return true;
  }

}
