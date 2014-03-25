package com.dgex.offspring.nxtCore.service;

import java.util.List;

import nxt.Account;
import nxt.Transaction;

public interface INxtDB {

  /**
   * Returns an ordered list of all transactions where fee paid or amount
   * received or amount send is greater than 0.
   * 
   * The goal of this list is to populate the transactions table in the account
   * section.
   * 
   * @param account
   * @return
   */
  List<Transaction> getBalanceAlteringTransactionsForAccount(Account account);

  List<Transaction> getBalanceAlteringTransactionsForAccount(Account a,
      int currentPage, int pageSize);

  int getBalanceAlteringTransactionsForAccountCount(Account a);

  /**
   * Returns an ordered list of all messages where account is either receiver or
   * sender.
   * 
   * The goal of this list is to populate the messages table in the account
   * section
   * 
   * @param account
   * @return
   */
  List<Transaction> getMessageTransactionsForAccount(Account account);

  /**
   * Returns an ordered list of all alias assignment transactions for this
   * account.
   * 
   * The goal of this list is to populate the alias table in the account section
   * 
   * @param account
   * @return
   */
  List<Transaction> getAliasTransactionsForAccount(Account account);

  /**
   * Returns an ordered list of IAsset objects of which you either have a
   * confirmed or unconfirmed balance higher than 0 or assets that you issued.
   * 
   * The goal of this list is to populate the asset table in the account section
   * 
   * @param account
   * @return
   */
  List<IAsset> getAssetsHoldByAccount(Account account);

}
