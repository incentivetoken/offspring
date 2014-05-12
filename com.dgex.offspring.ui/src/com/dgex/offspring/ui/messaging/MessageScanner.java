package com.dgex.offspring.ui.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nxt.Account;
import nxt.Nxt;
import nxt.Transaction;
import nxt.TransactionType;
import nxt.util.DbIterator;

import com.dgex.offspring.nxtCore.core.TransactionDB;
import com.dgex.offspring.nxtCore.service.INxtService;

public class MessageScanner {

  /**
   * The assumption is that all messages are scanned in order, whenever a
   * message is scanned that references another transaction, that other
   * transaction must be scanned before the current transaction.
   * 
   * Presentation in the tree is such that 'root' messages are sorted
   * ASCENDENING, meaning the most recent message is at the top.
   * 
   * Sub messages (messages in reply to another message) are sorted in
   * DESCENDING order, meaning that the reply to a message always comes below
   * the message that was replied to.
   */

  private final IMessageNode rootNode = new MessageNodeImpl(null, null);
  private final Map<Long, IMessageNode> rootMap = new HashMap<Long, IMessageNode>();
  private final Account account;
  private final String secretPhrase;
  private final INxtService nxt;

  public MessageScanner(Account account, String secretPhrase, INxtService nxt) {
    this.account = account;
    this.secretPhrase = secretPhrase;
    this.nxt = nxt;
  }

  public IMessageNode getNode() {
    return rootNode;
  }

  public void scan() {
    /* Scan the database */
    TransactionType[] types = { TransactionType.Messaging.ARBITRARY_MESSAGE };
    DbIterator<? extends Transaction> iterator = TransactionDB
        .getTransactionIterator(account.getId(), types, types, 0, Boolean.TRUE,
            null);
    while (iterator.hasNext()) {
      processTransaction(iterator.next());
    }

    /* Add all pending transactions not yet in the database */
    addPendingTransactions();
  }

  private void addPendingTransactions() {

    /* Collect all pending Transactions that match */
    List<Transaction> pending = new ArrayList<Transaction>();
    for (Transaction t : nxt.getPendingTransactions()) {
      if (account.getId().equals(t.getSenderId())
          || account.getId().equals(t.getRecipientId())) {
        if (t.getType().equals(TransactionType.Messaging.ARBITRARY_MESSAGE)) {
          pending.add(t);
        }
      }
    }

    if (pending.size() == 0)
      return;

    /* Collect pending transtactions that should no longer be pending */
    List<Transaction> remove = new ArrayList<Transaction>();
    List<Transaction> really_pending = new ArrayList<Transaction>();
    for (Transaction t : pending) {
      if (rootMap.containsKey(t.getId())) {
        remove.add(t);
      }
      else {
        really_pending.add(t);
      }
    }

    /* Remove no-longer pending transactions */
    for (Transaction t : remove) {
      nxt.getPendingTransactions().remove(t);
      pending.remove(t);
    }

    /* Add all really pending transactions */
    for (Transaction t : really_pending) {
      processTransaction(t);
    }
  }

  private Long getReferencedTransactionId(Transaction transaction) {
    if (transaction.getReferencedTransactionFullHash() != null) {
      Transaction ref = Nxt.getBlockchain().getTransactionByFullHash(
          transaction.getReferencedTransactionFullHash());
      if (ref != null) {
        return ref.getId();
      }
    }
    return null;
  }

  private void processTransaction(Transaction transaction) {
    IMessageNode node;
    Long id = getReferencedTransactionId(transaction);

    if (id == null) {
      node = new MessageNodeImpl(rootNode, new MessageWrapper(transaction,
          account, secretPhrase));
      rootNode.getChildren().add(0, node); // sorted ASCENDENING
    }
    else {
      IMessageNode referencedNode = rootMap.get(id);
      if (referencedNode == null) {
        node = new MessageNodeImpl(rootNode, new MessageWrapper(transaction,
            account, secretPhrase));
        rootNode.getChildren().add(0, node); // sorted ASCENDENING
      }
      else {
        node = new MessageNodeImpl(referencedNode, new MessageWrapper(
            transaction,
            account, secretPhrase));
        referencedNode.getChildren().add(node); // sorted DESCENDING
      }
    }
    rootMap.put(transaction.getId(), node);
  }
}
