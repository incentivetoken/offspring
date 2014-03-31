package com.dgex.offspring.nxtCore.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nxt.Db;
import nxt.Nxt;
import nxt.Transaction;
import nxt.TransactionType;
import nxt.util.DbIterator;
import nxt.util.DbUtils;

import org.apache.log4j.Logger;

import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.ITransaction;

public class TransactionDB {

  static Logger logger = Logger.getLogger(TransactionDB.class);

  /**
   * LazyList contains a DbIterator that needs to be closed after usage. In case
   * the iterator reaches the end (next() returns null) it will auto close
   * itself.
   * 
   * If on the other hand an exception happens the user is responsible for
   * closing the connection by calling dispose() on the LazyList
   */
  public static class LazyList {

    private final DbIterator<? extends Transaction> dbIterator;
    private final List<ITransaction> list = new ArrayList<ITransaction>();
    private final int available;
    private final INxtService nxt;

    public LazyList(DbIterator<? extends Transaction> dbIterator,
        int available, INxtService nxt) {
      this.dbIterator = dbIterator;
      this.available = available;
      this.nxt = nxt;
    }

    public void ensureCapacity(int size) {
      while (size > list.size() && dbIterator.hasNext()) {
        list.add(new TransactionHelper(nxt, dbIterator.next()));
      }
    }

    public List<ITransaction> getList() {
      return list;
    }

    public int available() {
      return available;
    }

    public void dispose() {
      logger.info("LazyList.dispose");
      dbIterator.close();
    }
  }

  public static LazyList getTransactions(Boolean orderAscending, INxtService nxt) {
    Connection con = null;

    /* Count number of records */
    int available = Nxt.getBlockchain().getTransactionCount();

    /* Create the record iterator */
    try {
      con = Db.getConnection();
      StringBuilder buf = new StringBuilder();
      buf.append("SELECT * FROM transaction ");
      if (orderAscending != null) {
        if (Boolean.TRUE.equals(orderAscending)) {
          buf.append("ORDER BY db_id ASC");
        }
        else if (Boolean.FALSE.equals(orderAscending)) {
          buf.append("ORDER BY db_id DESC");
        }
      }
      PreparedStatement pstmt = con.prepareStatement(buf.toString());
      DbIterator<? extends Transaction> iterator = Nxt.getBlockchain()
          .getTransactions(con, pstmt);
      LazyList lazyList = new LazyList(iterator, available, nxt);
      return lazyList;
    }
    catch (SQLException e) {
      logger.error("SQL Error", e);
      if (con != null) {
        DbUtils.close(con);
      }
    }
    return null;
  }

  /**
   * Returns a LazyList that holds an iterator to fill it's internal list and
   * knows up front how many elements the iterator contains.
   * 
   * @param accountId
   * @param recipientTypes
   * @param senderTypes
   * @param timestamp
   * @param orderAscending
   * @param referencedTransactionNull
   * @param nxt
   * @return
   */
  public static LazyList getTransactions(Long accountId,
      TransactionType[] recipientTypes, TransactionType[] senderTypes,
      int timestamp, Boolean orderAscending, Object referencedTransaction,
      INxtService nxt) {

    Connection con = null;
    int available = 0;

    /* Count number of records */
    try {
      con = Db.getConnection();
      PreparedStatement pstmt = createTransactionStatement(con, accountId,
          recipientTypes, senderTypes, timestamp, orderAscending,
          referencedTransaction, true);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      available = rs.getInt(1);
      DbUtils.close(con);
      con = null;
    }
    catch (SQLException e) {
      logger.error("SQL Error", e);
      return null;
    }
    finally {
      if (con != null) {
        DbUtils.close(con);
      }
    }

    /* Create the record iterator */
    try {
      con = Db.getConnection();
      PreparedStatement pstmt = createTransactionStatement(con, accountId,
          recipientTypes, senderTypes, timestamp, orderAscending,
          referencedTransaction, false);

      DbIterator<? extends Transaction> iterator = Nxt.getBlockchain()
          .getTransactions(con, pstmt);
      LazyList lazyList = new LazyList(iterator, available, nxt);
      return lazyList;
    }
    catch (SQLException e) {
      logger.error("SQL Error", e);
      if (con != null) {
        DbUtils.close(con);
      }
    }
    return null;
  }

  public static DbIterator<? extends Transaction> getTransactionIterator(
      Long accountId, TransactionType[] recipientTypes,
      TransactionType[] senderTypes, int timestamp, Boolean orderAscending,
      Object referencedTransaction) {
    Connection con = null;
    try {
      con = Db.getConnection();
      PreparedStatement pstmt = createTransactionStatement(con, accountId,
          recipientTypes, senderTypes, timestamp, orderAscending,
          referencedTransaction, false);

      DbIterator<? extends Transaction> iterator = Nxt.getBlockchain()
          .getTransactions(con, pstmt);
      return iterator;
    }
    catch (SQLException e) {
      logger.error("SQL Error", e);
      if (con != null) {
        DbUtils.close(con);
      }
    }
    return null;
  }

  public static List<ITransaction> getMessageTransactions(Long accountId,
      int timestamp, Boolean orderAscending, Object referencedTransaction,
      INxtService nxt) {

    Connection con = null;
    TransactionType[] recipientTypes = { TransactionType.Messaging.ARBITRARY_MESSAGE };
    TransactionType[] senderTypes = { TransactionType.Messaging.ARBITRARY_MESSAGE };

    /* Create the record iterator */
    try {
      con = Db.getConnection();
      PreparedStatement pstmt = createTransactionStatement(con, accountId,
          recipientTypes, senderTypes, timestamp, orderAscending,
          referencedTransaction, false);

      DbIterator<? extends Transaction> iterator = Nxt.getBlockchain()
          .getTransactions(con, pstmt);
      List<ITransaction> list = new ArrayList<ITransaction>();

      int max = 200;
      int count = 0;
      while (iterator.hasNext()) {
        count++;
        list.add(new TransactionHelper(nxt, iterator.next()));
        if (count > max) {
          DbUtils.close(con);
          break;
        }
      }
      return list;
    }
    catch (SQLException e) {
      logger.error("SQL Error", e);
      if (con != null) {
        DbUtils.close(con);
      }
    }
    catch (Throwable e) {
      logger.error("TransactionDB Error", e);
      if (con != null) {
        DbUtils.close(con);
      }
      throw e;
    }
    return null;
  }

  public static int getMessageTransactionCount(Long accountId, int timestamp,
      Object referencedTransaction, INxtService nxt) {

    Connection con = null;
    TransactionType[] recipientTypes = { TransactionType.Messaging.ARBITRARY_MESSAGE };
    TransactionType[] senderTypes = { TransactionType.Messaging.ARBITRARY_MESSAGE };

    /* Create the record counter */
    try {
      con = Db.getConnection();
      PreparedStatement pstmt = createTransactionStatement(con, accountId,
          recipientTypes, senderTypes, timestamp, null,
          referencedTransaction, true);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      int count = rs.getInt(1);
      DbUtils.close(con);
      con = null;
      return count;
    }
    catch (SQLException e) {
      logger.error("SQL Error", e);
      if (con != null) {
        DbUtils.close(con);
      }
    }
    catch (Throwable e) {
      logger.error("TransactionDB Error", e);
      if (con != null) {
        DbUtils.close(con);
      }
      throw e;
    }
    return 0;
  }

  static PreparedStatement createTransactionStatement(Connection con,
      Long accountId, TransactionType[] recipientTypes,
      TransactionType[] senderTypes, int timestamp, Boolean orderAscending,
      Object referencedTransaction,
      boolean countOnly) throws SQLException {

    if (recipientTypes.length == 0 && senderTypes.length == 0) {
      throw new RuntimeException("You must at least pass 1 transaction type");
    }

    StringBuilder buf = new StringBuilder();
    if (countOnly) {
      buf.append("SELECT COUNT(*) FROM ( ");
    }
    else if (orderAscending != null) {
      buf.append("SELECT * FROM ( ");
    }
    if (recipientTypes.length > 0) {
      buf.append("SELECT * FROM transaction WHERE recipient_id = ? ");
      if (timestamp > 0) {
        buf.append("AND timestamp >= ? ");
      }
      if (referencedTransaction != null) {
        if (referencedTransaction.equals(Boolean.TRUE)) {
          buf.append("AND referenced_transaction_id IS NULL ");
        }
        else if (referencedTransaction.equals(Boolean.FALSE)) {
          buf.append("AND referenced_transaction_id IS NOT NULL ");
        }
        else {
          buf.append("AND referenced_transaction_id = ? ");
        }
      }
      if (recipientTypes.length > 0) {
        buf.append("AND ( ");
        for (int i = 0; i < recipientTypes.length; i++) {
          buf.append("(type = ? AND subtype = ?) ");
          if (i < recipientTypes.length - 1) {
            buf.append("OR ");
          }
        }
        buf.append(") ");
      }
      if (senderTypes.length > 0) {
        buf.append("UNION ");
      }
    }
    if (senderTypes.length > 0) {
      buf.append("SELECT * FROM transaction WHERE sender_id = ? ");
      if (timestamp > 0) {
        buf.append("AND timestamp >= ? ");
      }
      if (referencedTransaction != null) {
        if (referencedTransaction.equals(Boolean.TRUE)) {
          buf.append("AND referenced_transaction_id IS NULL ");
        }
        else if (referencedTransaction.equals(Boolean.FALSE)) {
          buf.append("AND referenced_transaction_id IS NOT NULL ");
        }
        else {
          buf.append("AND referenced_transaction_id = ? ");
        }
      }
      if (senderTypes.length > 0) {
        buf.append("AND ( ");
        for (int i = 0; i < senderTypes.length; i++) {
          buf.append("(type = ? AND subtype = ?) ");
          if (i < senderTypes.length - 1) {
            buf.append("OR ");
          }
        }
        buf.append(") ");
      }
    }
    if (countOnly) {
      buf.append(")");
    }
    else {
      if (Boolean.TRUE.equals(orderAscending)) {
        buf.append(") ORDER BY timestamp ASC");
      }
      else if (Boolean.FALSE.equals(orderAscending)) {
        buf.append(") ORDER BY timestamp DESC");
      }
    }

    // logger.info(buf.toString());
    int i = 0;
    PreparedStatement pstmt = con.prepareStatement(buf.toString());

    if (recipientTypes.length > 0) {
      pstmt.setLong(++i, accountId);
      if (timestamp > 0) {
        pstmt.setInt(++i, timestamp);
      }
      if (referencedTransaction instanceof Long) {
        pstmt.setLong(++i, (long) referencedTransaction);
      }
      if (recipientTypes.length > 0) {
        for (int j = 0; j < recipientTypes.length; j++) {
          TransactionType type = recipientTypes[j];
          pstmt.setByte(++i, type.getType());
          pstmt.setByte(++i, type.getSubtype());
        }
      }
    }
    if (senderTypes.length > 0) {
      pstmt.setLong(++i, accountId);
      if (timestamp > 0) {
        pstmt.setInt(++i, timestamp);
      }
      if (referencedTransaction instanceof Long) {
        pstmt.setLong(++i, (long) referencedTransaction);
      }
      if (senderTypes.length > 0) {
        for (int j = 0; j < senderTypes.length; j++) {
          TransactionType type = senderTypes[j];
          pstmt.setByte(++i, type.getType());
          pstmt.setByte(++i, type.getSubtype());
        }
      }
    }
    // logger.info(pstmt.toString());
    return pstmt;
  }
}
