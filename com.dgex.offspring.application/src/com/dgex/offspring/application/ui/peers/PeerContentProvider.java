package com.dgex.offspring.application.ui.peers;

import java.util.List;

import nxt.peer.Peer;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.dgex.offspring.nxtCore.service.INxtService;

public class PeerContentProvider implements IStructuredContentProvider {

  private final int peerType;

  private INxtService nxt;

  public PeerContentProvider(int peerType) {
    this.peerType = peerType;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    this.nxt = (INxtService) newInput;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (nxt == null) { return new Object[0]; }

    List<Peer> peers = null;
    if (peerType == PeerTable.TYPE_ACTIVE_PEERS) {
      peers = nxt.getAllConnectedPeers();
    }
    else if (peerType == PeerTable.TYPE_KNOWN_PEERS) {
      peers = nxt.getAllWellknownPeers();
    }
    else if (peerType == PeerTable.TYPE_BLACKLISTED_PEERS) {
      peers = nxt.getAllBlacklistedPeers();
    }

    if (peers == null) { return new Object[0]; }
    return peers.toArray(new Object[peers.size()]);
  }

  @Override
  public void dispose() {
    nxt = null;
  }
}
