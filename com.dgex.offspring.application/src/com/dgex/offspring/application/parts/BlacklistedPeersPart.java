package com.dgex.offspring.application.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.peer.Peer;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.application.ui.peers.PeerTable;
import com.dgex.offspring.application.ui.peers.PeerTableViewer;
import com.dgex.offspring.nxtCore.service.INxtService;

public class BlacklistedPeersPart {

  private PeerTableViewer peerTableViewer;

  @PostConstruct
  public void postConstruct(Composite parent, INxtService nxt) {
    peerTableViewer = new PeerTableViewer(parent, nxt,
        PeerTable.TYPE_BLACKLISTED_PEERS);
  }

  @Focus
  public void onFocus() {
    peerTableViewer.getControl().setFocus();
  }

  @Inject
  @Optional
  private void onBlacklistPeerEvent(
      @UIEventTopic(INxtService.TOPIC_BLACKLIST_PEER) Peer peer) {
    if (peerTableViewer != null) {
      peerTableViewer.refresh();
    }
  }

  @Inject
  @Optional
  private void onUnblacklistPeerEvent(
      @UIEventTopic(INxtService.TOPIC_UNBLACKLIST_PEER) Peer peer) {
    if (peerTableViewer != null) {
      peerTableViewer.refresh();
    }
  }
}