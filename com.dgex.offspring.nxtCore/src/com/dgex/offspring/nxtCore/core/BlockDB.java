package com.dgex.offspring.nxtCore.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nxt.Block;
import nxt.Db;
import nxt.Nxt;
import nxt.util.DbIterator;
import nxt.util.DbUtils;

import org.apache.log4j.Logger;

import com.dgex.offspring.nxtCore.service.INxtService;

public class BlockDB {

  static Logger logger = Logger.getLogger(BlockDB.class);

  /**
   * LazyList contains a DbIterator that needs to be closed after usage. In case
   * the iterator reaches the end (next() returns null) it will auto close
   * itself.
   * 
   * If on the other hand an exception happens the user is responsible for
   * closing the connection by calling dispose() on the LazyList
   */
  public static class LazyList {

    private final DbIterator<? extends Block> dbIterator;
    private final List<Block> list = new ArrayList<Block>();
    private final int available;

    public LazyList(DbIterator<? extends Block> dbIterator, int available) {
      this.dbIterator = dbIterator;
      this.available = available;
    }

    public void ensureCapacity(int size) {
      while (size > list.size() && dbIterator.hasNext()) {
        list.add(dbIterator.next());
      }
    }

    public List<Block> getList() {
      return list;
    }

    public int available() {
      return available;
    }

    public void dispose() {
      dbIterator.close();
    }
  }

  public static LazyList getBlocks(Boolean orderAscending, INxtService nxt) {
    Connection con = null;

    /* Count number of records */
    // int available = nxt.getSmartBlockCount();
    int available = Nxt.getBlockchain().getLastBlock().getHeight();

    /* Create the record iterator */
    try {
      con = Db.getConnection();
      StringBuilder buf = new StringBuilder();
      buf.append("SELECT * FROM block ");
      if (orderAscending != null) {
        if (Boolean.TRUE.equals(orderAscending)) {
          buf.append("ORDER BY db_id ASC");
        }
        else if (Boolean.FALSE.equals(orderAscending)) {
          buf.append("ORDER BY db_id DESC");
        }
      }
      PreparedStatement pstmt = con.prepareStatement(buf.toString());
      DbIterator<? extends Block> iterator = Nxt.getBlockchain().getBlocks(con,
          pstmt);
      LazyList lazyList = new LazyList(iterator, available);
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

  public static List<Block> getGeneratedBlocks(Long accountId) {
    Connection con = null;
    try {
      con = Db.getConnection();
      PreparedStatement pstmt = con
          .prepareStatement("SELECT * FROM block WHERE generator_id = ? ORDER BY db_id ASC");
      pstmt.setLong(1, accountId);

      List<Block> result = new ArrayList<Block>();
      DbIterator<? extends Block> iterator = Nxt.getBlockchain().getBlocks(con,
          pstmt);
      while (iterator.hasNext()) {
        result.add(iterator.next());
      }
      return result;
    }
    catch (SQLException e) {
      DbUtils.close(con);
      throw new RuntimeException(e.toString(), e);
    }
  }
}
