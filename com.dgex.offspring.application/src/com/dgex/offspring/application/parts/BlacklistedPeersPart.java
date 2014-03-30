package com.dgex.offspring.application.parts;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.peer.Peer;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.dgex.offspring.application.ui.peers.PeerTable;
import com.dgex.offspring.application.ui.peers.PeerTableViewer;
import com.dgex.offspring.nxtCore.service.INxtService;

public class BlacklistedPeersPart {

  private PeerTableViewer peerTableViewer;

  @PostConstruct
  public void postConstruct(Composite parent, INxtService nxt) {
    peerTableViewer = new PeerTableViewer(parent, nxt,
        PeerTable.TYPE_BLACKLISTED_PEERS);

    Menu contextMenu = new Menu(peerTableViewer.getTable());
    peerTableViewer.getTable().setMenu(contextMenu);

    MenuItem itemReply = new MenuItem(contextMenu, SWT.PUSH);
    itemReply.setText("Unblacklist");
    itemReply.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) peerTableViewer
            .getSelection();

        Iterator iter = selection.iterator();
        while (iter != null && iter.hasNext()) {
          Object element = iter.next();
          if (element instanceof Peer) {
            Peer peer = (Peer) element;
            peer.unBlacklist();
          }
        }
        peerTableViewer.refresh();
      }
    });
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