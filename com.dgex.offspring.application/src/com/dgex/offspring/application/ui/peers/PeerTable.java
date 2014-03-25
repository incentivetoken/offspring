package com.dgex.offspring.application.ui.peers;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.SWT;

import com.dgex.offspring.messages.Messages;

public class PeerTable {

  public static final int TYPE_ACTIVE_PEERS = 0;
  public static final int TYPE_KNOWN_PEERS = 1;
  public static final int TYPE_BLACKLISTED_PEERS = 2;

  public final static int COLUMN_WELLKNOWN = 34;
  public final static int COLUMN_ANNOUNCED_ADDRESS = 35;
  public final static int COLUMN_DISCONNECTED = 36;
  public final static int COLUMN_ADDRESS = 37;
  public final static int COLUMN_WEIGHT = 38;
  public final static int COLUMN_DOWNLOADED = 39;
  public final static int COLUMN_UPLOADED = 40;
  public final static int COLUMN_SOFTWARE = 41;
  public final static int COLUMN_REMOVE_BUTTON = 42;

  private final static int[] active_peers_columns = new int[] {

  COLUMN_REMOVE_BUTTON,

  COLUMN_DISCONNECTED,

  COLUMN_ADDRESS,

  COLUMN_WEIGHT,

  COLUMN_DOWNLOADED,

  COLUMN_UPLOADED,

  COLUMN_SOFTWARE };

  private final static int[] known_peers_columns = new int[] {

  COLUMN_REMOVE_BUTTON,

  COLUMN_ANNOUNCED_ADDRESS,

  COLUMN_WEIGHT,

  COLUMN_DOWNLOADED,

  COLUMN_UPLOADED,

  COLUMN_SOFTWARE };

  private final static int[] blacklisted_peers_columns = new int[] {

  COLUMN_REMOVE_BUTTON,

  COLUMN_ANNOUNCED_ADDRESS,

  COLUMN_WEIGHT,

  COLUMN_DOWNLOADED,

  COLUMN_UPLOADED,

  COLUMN_SOFTWARE };

  public static int[] getColumns(int peerType) {
    switch (peerType) {
    case TYPE_ACTIVE_PEERS:
      return active_peers_columns;
    case TYPE_KNOWN_PEERS:
      return known_peers_columns;
    case TYPE_BLACKLISTED_PEERS:
      return blacklisted_peers_columns;
    }
    return null;
  }

  public static CellLabelProvider createLabelProvider(int id, int peerType) {
    switch (id) {
    case COLUMN_REMOVE_BUTTON:
    case COLUMN_DISCONNECTED:
    case COLUMN_WELLKNOWN:
    case COLUMN_ANNOUNCED_ADDRESS:
    case COLUMN_ADDRESS:
    case COLUMN_WEIGHT:
    case COLUMN_SOFTWARE:
    case COLUMN_DOWNLOADED:
    case COLUMN_UPLOADED:
      return new PeerLabelProvider(peerType);

      /*
       * case COLUMN_DOWNLOADED: case COLUMN_UPLOADED: return new
       * PeerStyledLabelProvider(peerType);
       */
    }
    return null;
  }

  public static String getColumnLabel(int id) {
    switch (id) {
    case COLUMN_REMOVE_BUTTON:
      return " "; //$NON-NLS-1$
    case COLUMN_DISCONNECTED:
      return " "; //$NON-NLS-1$
    case COLUMN_WELLKNOWN:
      return " "; //$NON-NLS-1$
    case COLUMN_ANNOUNCED_ADDRESS:
      return Messages.PeerTable_column_announced_address_label;
    case COLUMN_ADDRESS:
      return Messages.PeerTable_column_address_label;
    case COLUMN_WEIGHT:
      return Messages.PeerTable_column_weight_label;
    case COLUMN_DOWNLOADED:
      return Messages.PeerTable_column_down_label;
    case COLUMN_UPLOADED:
      return Messages.PeerTable_column_up_label;
    case COLUMN_SOFTWARE:
      return Messages.PeerTable_column_software_label;
    }
    return "FAILURE"; //$NON-NLS-1$
  }

  public static int getColumnWidth(int id) {
    switch (id) {
    case COLUMN_REMOVE_BUTTON:
    case COLUMN_DISCONNECTED:
    case COLUMN_WELLKNOWN:
      return 30;
    case COLUMN_ANNOUNCED_ADDRESS:
    case COLUMN_ADDRESS:
      return 200;
    case COLUMN_WEIGHT:
    case COLUMN_DOWNLOADED:
    case COLUMN_UPLOADED:
      return 75;
    case COLUMN_SOFTWARE:
      return 150;
    }
    return 10;
  }

  public static int getColumnAlignment(int id) {
    switch (id) {
    case COLUMN_REMOVE_BUTTON:
    case COLUMN_WELLKNOWN:
    case COLUMN_DISCONNECTED:
      return SWT.CENTER;
    case COLUMN_WEIGHT:
    case COLUMN_DOWNLOADED:
    case COLUMN_UPLOADED:
      return SWT.RIGHT;
    }
    return SWT.LEFT;
  }

  public static boolean getColumnResizable(int id) {
    switch (id) {
    case COLUMN_REMOVE_BUTTON:
    case COLUMN_DISCONNECTED:
    case COLUMN_WELLKNOWN:
    case COLUMN_WEIGHT:
    case COLUMN_DOWNLOADED:
    case COLUMN_UPLOADED:
      return false;
    }
    return true;
  }
}
