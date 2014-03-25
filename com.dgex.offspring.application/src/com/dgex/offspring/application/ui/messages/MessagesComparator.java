package com.dgex.offspring.application.ui.messages;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.nxtCore.service.IMessage;
import com.dgex.offspring.user.service.IUser;

public class MessagesComparator extends ViewerComparator {

  private int columnID;
  private static final int DESCENDING = 1;
  private int direction = DESCENDING;
  private final IUser user;

  public MessagesComparator(IUser user) {
    this.columnID = MessagesTable.COLUMN_TIMESTAMP;
    direction = DESCENDING;
    this.user = user;
  }

  public int getDirection() {
    return direction == 1 ? SWT.DOWN : SWT.UP;
  }

  public void setColumn(int columnID) {
    if (columnID == this.columnID) {
      direction = 1 - direction;
    }
    else {
      this.columnID = columnID;
      direction = DESCENDING;
    }
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    IMessage a1 = (IMessage) e1;
    IMessage a2 = (IMessage) e2;
    int rc = 0;
    switch (columnID) {

    case MessagesTable.COLUMN_TIMESTAMP:
      rc = CompareMe.compare(a1.getTimestamp(), a2.getTimestamp());
      break;

    case MessagesTable.COLUMN_ID:
      rc = CompareMe.compare(a1.getId(), a2.getId());
      break;

    case MessagesTable.COLUMN_RECIPIENT:
      rc = CompareMe.compare(a1.getRecipient().getId(), a2.getRecipient()
          .getId());
      break;

    case MessagesTable.COLUMN_MESSAGE:
      rc = CompareMe.compare(a1.getMessage(), a2.getMessage());
      break;

    default:
      rc = 0;
    }
    // If descending order, flip the direction
    if (direction == DESCENDING) {
      rc = -rc;
    }
    return rc;
  }
}
