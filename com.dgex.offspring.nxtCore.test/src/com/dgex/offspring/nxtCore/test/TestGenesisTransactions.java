package com.dgex.offspring.nxtCore.test;

import static org.junit.Assert.assertTrue;
import nxt.Account;
import nxt.Nxt;
import nxt.Transaction;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import nxt.util.DbIterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dgex.offspring.nxtCore.service.Utils;

public class TestGenesisTransactions {

  @BeforeClass
  public static void init() {
    NxtLoader.init();
  }

  @AfterClass
  public static void shutdown() {
    NxtLoader.shutdown();
  }

  Account getGenesisAccount() {
    byte[] key = Crypto
        .getPublicKey("It was a bright cold day in April, and the clocks were striking thirteen.");
    return Account.getAccount(key);
  }

  @Test
  public void testGenesisAccountAccess() {
    assertTrue(getGenesisAccount() instanceof Account);
  }

  @Test
  public void testListGenesisTransactions() {
    Account account = getGenesisAccount();
    DbIterator<? extends Transaction> iter = Nxt.getBlockchain()
        .getTransactions(account, (byte) -1, (byte) -1, 0);

    long totalAmount = 0;
    while (iter.hasNext()) {
      Transaction txn = iter.next();
      String recipient = Convert.toUnsignedLong(txn.getRecipientId());
      totalAmount += txn.getAmountNQT();
      System.out.println(recipient + " => "
          + Utils.quantToString(txn.getAmountNQT(), 8)
          + "("
          + totalAmount + ")");
    }
  }

}
