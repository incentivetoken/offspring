package com.dgex.offspring.application.ui.peers;

import nxt.peer.Peer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import com.dgex.offspring.config.CompareMe;

public class PeerComparator extends ViewerComparator {

  private int columnID;

  private static final int DESCENDING = 1;

  private static final int ASSCENDING = 0;

  private int direction = DESCENDING;

  public PeerComparator() {
    this.columnID = PeerTable.COLUMN_ADDRESS;
    // direction = DESCENDING;
    direction = ASSCENDING;
  }

  public int getDirection() {
    return direction == 1 ? SWT.DOWN : SWT.UP;
  }

  public void setColumn(int columnID) {
    if (columnID == this.columnID) {
      // Same column as last sort; toggle the direction
      direction = 1 - direction;
    }
    else {
      // New column; do an ascending sort
      this.columnID = columnID;
      direction = DESCENDING;
    }
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    Peer p1 = (Peer) e1;
    Peer p2 = (Peer) e2;
    int rc = 0;
    switch (columnID) {
    case PeerTable.COLUMN_DISCONNECTED:
      rc = CompareMe.compare(p1.getState(), p2.getState());
      break;
    case PeerTable.COLUMN_WELLKNOWN:
      rc = CompareMe.compare(p1.isWellKnown(), p2.isWellKnown());
      break;
    case PeerTable.COLUMN_ANNOUNCED_ADDRESS:
      rc = CompareMe
          .compare(p1.getAnnouncedAddress(), p2.getAnnouncedAddress());
      break;
    case PeerTable.COLUMN_ADDRESS:
      rc = CompareMe.compare(p1.getPeerAddress(), p2.getPeerAddress());
      break;
    case PeerTable.COLUMN_WEIGHT:
      rc = CompareMe.compare(p1.getWeight(), p2.getWeight());
      break;
    case PeerTable.COLUMN_DOWNLOADED:
      rc = CompareMe
          .compare(p1.getDownloadedVolume(), p2.getDownloadedVolume());
      break;
    case PeerTable.COLUMN_UPLOADED:
      rc = CompareMe.compare(p1.getUploadedVolume(), p2.getUploadedVolume());
      break;
    case PeerTable.COLUMN_SOFTWARE:
      rc = CompareMe.compare(p1.getSoftware(), p2.getSoftware());
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
