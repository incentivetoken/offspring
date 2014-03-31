package com.dgex.offspring.application.parts;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.peer.Peer;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.application.ui.peers.PeerTable;
import com.dgex.offspring.application.ui.peers.PeerTableViewer;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.ui.SendMessageWizard;
import com.dgex.offspring.user.service.IUserService;

public class ActivePeersPart {

  private PeerTableViewer peerTableViewer;

  @PostConstruct
  public void postConstruct(final Composite parent, final INxtService nxt,
      final IUserService userService) {
    peerTableViewer = new PeerTableViewer(parent, nxt,
        PeerTable.TYPE_ACTIVE_PEERS);

    Menu contextMenu = new Menu(peerTableViewer.getTable());
    peerTableViewer.getTable().setMenu(contextMenu);

    MenuItem item = new MenuItem(contextMenu, SWT.PUSH);
    item.setText("Blacklist");
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) peerTableViewer
            .getSelection();

        Iterator iter = selection.iterator();
        while (iter != null && iter.hasNext()) {
          Object element = iter.next();
          if (element instanceof Peer) {
            Peer peer = (Peer) element;
            peer.blacklist();
          }
        }
        peerTableViewer.refresh();
      }
    });

    item = new MenuItem(contextMenu, SWT.PUSH);
    item.setText("Send Message");
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) peerTableViewer
            .getSelection();
        Object element = selection.getFirstElement();
        if (element instanceof Peer) {
          Peer peer = (Peer) element;
          if (peer.getHallmark() != null) {

            Shell shell = parent.getShell();
            if (shell != null) {
              while (shell.getParent() != null) {
                shell = shell.getParent().getShell();
              }
            }
            WizardDialog dialog = new WizardDialog(shell,
                new SendMessageWizard(userService, nxt, peer.getHallmark()
                    .getAccountId(), null, true));
            dialog.open();
          }
        }
      }
    });
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