package com.dgex.offspring.application.ui.peers;

import nxt.peer.Peer;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

import com.dgex.offspring.config.Formatter;
import com.dgex.offspring.config.Images;

public class PeerLabelProvider extends ColumnLabelProvider {

  private static final String EMPTY_STRING = "";

  private final int peerType;

  private static final Image CONNECTED = Images.getImage("connected.png");

  private static final Image WELLKNOWN_PEER = Images.getImage("user_suit.png");

  private static final Image ANNOUNCED_PEER = Images.getImage("user.png");

  private static final Image HALLMARK = Images.getImage("shield.png");

  private static final Image NO_HALLMARK = Images.getImage("shield_delete.png");

  public PeerLabelProvider(int peerType) {
    this.peerType = peerType;
  }

  @Override
  public void update(ViewerCell cell) {
    super.update(cell);
    Peer p = (Peer) cell.getElement();
    switch (PeerTable.getColumns(peerType)[cell.getColumnIndex()]) {

    case PeerTable.COLUMN_HALLMARK_BUTTON:
      if (p.getHallmark() == null) {
        cell.setImage(NO_HALLMARK);
      }
      else {
        cell.setImage(HALLMARK);
      }
      cell.setText(EMPTY_STRING);
      break;

    case PeerTable.COLUMN_DISCONNECTED:
      if (p.getState() != Peer.State.DISCONNECTED
          && p.getState() != Peer.State.NON_CONNECTED) {
        cell.setImage(CONNECTED);
      }
      cell.setText(EMPTY_STRING);
      break;

    case PeerTable.COLUMN_WELLKNOWN:
      if (p.isWellKnown()) {
        cell.setImage(WELLKNOWN_PEER);
      }
      else {
        cell.setImage(ANNOUNCED_PEER);
      }
      cell.setText(EMPTY_STRING);
      break;

    case PeerTable.COLUMN_ANNOUNCED_ADDRESS:
      if (p.isWellKnown()) {
        cell.setImage(WELLKNOWN_PEER);
      }
      else {
        cell.setImage(ANNOUNCED_PEER);
      }
      cell.setText(p.getAnnouncedAddress());
      break;

    case PeerTable.COLUMN_ADDRESS:
      if (p.isWellKnown()) {
        cell.setImage(WELLKNOWN_PEER);
      }
      else {
        cell.setImage(ANNOUNCED_PEER);
      }
      cell.setText(p.getPeerAddress());
      break;

    case PeerTable.COLUMN_WEIGHT:
      cell.setText(Integer.toString(p.getWeight()));
      break;

    case PeerTable.COLUMN_SOFTWARE:
      cell.setText(p.getSoftware());
      break;

    case PeerTable.COLUMN_DOWNLOADED:
      cell.setText(Formatter.readableFileSize(p.getDownloadedVolume()));
      break;

    case PeerTable.COLUMN_UPLOADED:
      cell.setText(Formatter.readableFileSize(p.getUploadedVolume()));
      break;

    default:
      cell.setText("UNKNOWN "
          + PeerTable.getColumns(peerType)[cell.getColumnIndex()]);
    }
  }
}
