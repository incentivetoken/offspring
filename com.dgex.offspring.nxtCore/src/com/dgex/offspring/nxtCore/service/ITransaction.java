package com.dgex.offspring.nxtCore.service;

import java.util.Date;

import nxt.Transaction;

public interface ITransaction {

  public Transaction getNative();

  public int getAmount();

  public int getFee();

  public String getStringId();

  public IAccount getSender();

  public IAccount getReceiver();

  public Date getTimestamp();

  public long getAmountReceived(Long accountId);

  public long getAmountSpend(Long accountId);

  public long getFeePaid(Long accountId);

  public long getRunningTotal();

  public int getNumberOfConfirmations();

}
