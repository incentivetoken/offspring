package com.dgex.offspring.nxtCore.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nxt.Constants;
import nxt.Nxt;
import nxt.Transaction;
import nxt.util.Convert;
import nxt.util.DbIterator;

import org.apache.log4j.Logger;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.ITransaction;

public class TransactionHelper implements ITransaction {

  static Logger logger = Logger.getLogger(TransactionHelper.class);

  protected final Transaction transaction;
  private long runningTotal;
  private final INxtService nxt;
  private IAccount sender = null;
  private IAccount receiver = null;
  private Date date = null;

  public static class IteratorAsList {

    private final DbIterator<? extends Transaction> iterator;
    private final List<ITransaction> list = new ArrayList<ITransaction>();
    private final INxtService nxt;

    public IteratorAsList(INxtService nxt,
        DbIterator<? extends Transaction> dbIterator) {
      this.nxt = nxt;
      this.iterator = dbIterator;
    }

    public List<ITransaction> getList(int length) {
      while (length > list.size() && iterator.hasNext()) {
        logger.info("getList we_need=" + length + " we_got=" + list.size());
        list.add(new TransactionHelper(nxt, iterator.next()));
      }
      return list;
    }

    public int size() {
      return list.size();
    }

    public boolean hasMore() {
      return iterator.hasNext();
    }
  }

  public TransactionHelper(INxtService nxt, Transaction transaction) {
    this.nxt = nxt;
    this.transaction = transaction;
  }

  public static IteratorAsList getIteratorAsList(INxtService nxt,
      DbIterator<Transaction> iterator) {
    return new IteratorAsList(nxt, iterator);
  }

  @Override
  public String toString() {
    return new StringBuilder().append("Txn ").append(getStringId())
        .append(" amount=").append(Convert.toNXT(getAmountNQT())).append(" fee=")
.append(Convert.toNXT(getFeeNQT())).append(" sender=")
        .append(getSender().getStringId())
        .append(" receiver=").append(getReceiver().getStringId())
        .append(" timestamp=").append(transaction.getTimestamp())
        .append(" epoch=").append(Convert.getEpochTime()).toString();
  }

  @Override
  public Transaction getNative() {
    return transaction;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof TransactionHelper) {
      if (((TransactionHelper) other).transaction.getId().equals(
          this.transaction.getId())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public long getAmountNQT() {
    return transaction.getAmountNQT();
  }

  @Override
  public long getFeeNQT() {
    return transaction.getFeeNQT();
  }

  @Override
  public String getStringId() {
    return transaction.getStringId();
  }

  @Override
  public IAccount getSender() {
    if (sender != null)
      return sender;
    return (sender = new AccountHelper(nxt, transaction.getSenderId()));
  }

  @Override
  public IAccount getReceiver() {
    if (receiver != null)
      return receiver;
    return (receiver = new AccountHelper(nxt, transaction.getRecipientId()));
  }

  /**
   * Convert a NXT timestamp to a *normal* Date, timestamp is the number of
   * seconds since NXT epoch.
   */
  @Override
  public Date getTimestamp() {
    if (date != null)
      return date;

    return (date = new Date(((transaction.getTimestamp()) * 1000l)
        + (Constants.EPOCH_BEGINNING - 500)));
  }

  @Override
  public long getAmountReceivedNQT(Long accountId) {
    long total = 0;
    if (accountId.equals(transaction.getSenderId())) {
      total -= transaction.getAmountNQT() + transaction.getFeeNQT();
    }
    if (accountId.equals(transaction.getRecipientId())) {
      total += transaction.getAmountNQT();
    }
    return total < 0 ? 0 : total;
  }

  @Override
  public long getAmountSpendNQT(Long accountId) {
    long total = 0;
    if (accountId.equals(transaction.getSenderId())) {
      total += transaction.getAmountNQT() + transaction.getFeeNQT();
    }
    if (accountId.equals(transaction.getRecipientId())) {
      total -= transaction.getAmountNQT();
    }
    return total < 0 ? 0 : total;
  }

  @Override
  public long getFeePaidNQT(Long accountId) {
    if (accountId.equals(transaction.getSenderId())) {
      return transaction.getFeeNQT();
    }
    return 0l;
  }

  @Override
  public long getRunningTotalNQT() {
    return runningTotal;
  }

  public void setRunningTotalNQT(long total) {
    runningTotal = total;
  }

  @Override
  public int getNumberOfConfirmations() {
    if (nxt.getPendingTransactions().indexOf(transaction) == -1) {
      return Nxt.getBlockchain().getLastBlock().getHeight()
          - transaction.getHeight();
    }
    return 0;
  }
}
