package com.dgex.offspring.nxtCore.core;

import java.util.ArrayList;
import java.util.List;

import nxt.Account;
import nxt.Nxt;
import nxt.Transaction;
import nxt.TransactionType;
import nxt.util.DbIterator;

import com.dgex.offspring.config.Paginator;
import com.dgex.offspring.nxtCore.service.IAsset;
import com.dgex.offspring.nxtCore.service.INxtDB;
import com.dgex.offspring.nxtCore.service.INxtService;

public class NxtDB implements INxtDB {

  static interface ITransactionFilter {

    boolean accept(Transaction transation);
  }

  private final INxtService nxt;

  public NxtDB(INxtService nxt) {
    this.nxt = nxt;
  }

  private void getAll(DbIterator<? extends Transaction> iterator,
      List<Transaction> output) {
    while (iterator.hasNext()) {
      output.add(iterator.next());
    }
  }

  private void getAll(DbIterator<? extends Transaction> iterator,
      List<Transaction> output, ITransactionFilter filter) {
    Transaction transation;
    while (iterator.hasNext()) {
      transation = iterator.next();
      if (filter.accept(transation))
        output.add(transation);
    }
  }

  @Override
  public int getBalanceAlteringTransactionsForAccountCount(Account a) {
    return 0; // Nxt.getBlockchain().getTransactionCount(a, (byte) -1, (byte)
              // -1, 0);
  }

  @Override
  public List<Transaction> getBalanceAlteringTransactionsForAccount(
      final Account a, int currentPage, int pageSize) {

    int limit = pageSize;
    int count = 0; // Nxt.getBlockchain().getTransactionCount(a, (byte) -1,
                   // (byte) -1, 0);
    Paginator paginator = new Paginator(currentPage, pageSize, count);
    int offset = paginator.getStartingIndex();

    List<Transaction> transactions = new ArrayList<Transaction>();
    getAll(
        Nxt.getBlockchain().getTransactions(a, (byte) -1, (byte) -1, 0,
            Boolean.TRUE), transactions, new ITransactionFilter() {

          @Override
          public boolean accept(Transaction t) {
            /* Filter out all messages that we received */
            if (t.getType().equals(TransactionType.Messaging.ARBITRARY_MESSAGE)) {
              return t.getSenderId().equals(a.getId());
            }
            return true;
          }
        });

    /*
     * Little weirdness so watch out .. We add all pending transactions to the
     * first page only. This causes the first page to have a bigger pagesize,
     * each pending transaction adds to the pagesize. The following pages ignore
     * the pending transactions.
     */
    if (currentPage == 1) {
      List<Transaction> remove = new ArrayList<Transaction>();
      for (Transaction t : nxt.getPendingTransactions()) {
        if (t.getSenderId().equals(a.getId())
            || t.getRecipientId().equals(a.getId())) {
          if (transactions.indexOf(t) != -1) {
            remove.add(t);
          }
          else {
            transactions.add(0, t);
          }
        }
      }
      for (Transaction t : remove) {
        nxt.getPendingTransactions().remove(t);
      }
    }
    return transactions;
  }

  @Override
  public List<Transaction> getBalanceAlteringTransactionsForAccount(
      final Account account) {

    byte TYPE_ALL = -1;
    List<Transaction> transactions = new ArrayList<Transaction>();

    /* Get all transactions where Account is either sender or receiver */

    getAll(Nxt.getBlockchain().getTransactions(account, TYPE_ALL, TYPE_ALL, 0),
        transactions, new ITransactionFilter() {

          TransactionType[] accepted = new TransactionType[] {
              TransactionType.Payment.ORDINARY,
              TransactionType.Messaging.ALIAS_ASSIGNMENT,
              TransactionType.ColoredCoins.ASSET_ISSUANCE,
              TransactionType.ColoredCoins.ASK_ORDER_PLACEMENT,
              TransactionType.ColoredCoins.BID_ORDER_PLACEMENT,
              TransactionType.ColoredCoins.ASK_ORDER_CANCELLATION,
              TransactionType.ColoredCoins.BID_ORDER_CANCELLATION };

          @Override
          public boolean accept(Transaction t) {
            for (TransactionType type : accepted) {
              if (type.equals(t.getType())) {
                return true;
              }
            }

            /* Includes all messages that WE send NOT messages received */
            if (t.getType().equals(TransactionType.Messaging.ARBITRARY_MESSAGE)) {
              return t.getSenderId().equals(account.getId());
            }
            return true;
          }
        });

    /* Add all pending transactions for this Account */

    List<Transaction> remove = new ArrayList<Transaction>();
    for (Transaction t : nxt.getPendingTransactions()) {
      if (t.getSenderId().equals(account.getId())
          || t.getRecipientId().equals(account.getId())) {
        if (transactions.indexOf(t) != -1) {
          remove.add(t);
        }
        else {
          transactions.add(0, t);
        }
      }
    }
    for (Transaction t : remove) {
      nxt.getPendingTransactions().remove(t);
    }

    return transactions;
  }

  @Override
  public List<Transaction> getMessageTransactionsForAccount(Account account) {
    List<Transaction> transactions = new ArrayList<Transaction>();

    return null;
  }

  @Override
  public List<Transaction> getAliasTransactionsForAccount(Account account) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<IAsset> getAssetsHoldByAccount(Account account) {
    // TODO Auto-generated method stub
    return null;
  }

}
