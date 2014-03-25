package com.dgex.offspring.application.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.peer.Peer;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.dgex.offspring.application.ui.peers.PeerTable;
import com.dgex.offspring.application.ui.peers.PeerTableViewer;
import com.dgex.offspring.nxtCore.service.INxtService;

public class ActivePeersPart {

  private PeerTableViewer peerTableViewer;

  @PostConstruct
  public void postConstruct(Composite parent, INxtService nxt) {
    peerTableViewer = new PeerTableViewer(parent, nxt,
        PeerTable.TYPE_ACTIVE_PEERS);
  }

  @Focus
  public void onFocus() {
    peerTableViewer.getControl().setFocus();
  }

  @Inject
  @Optional
  private void onPeerEvent(@UIEventTopic(INxtService.TOPIC_PEER) Peer peer) {
    if (peerTableViewer != null) {
      Control control = peerTableViewer.getControl();
      if (control != null && !control.isDisposed()) {
        peerTableViewer.refresh();
      }
    }
  }
}