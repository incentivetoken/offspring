package com.dgex.offspring.nxtCore.service;

import java.util.Date;

import nxt.Transaction;

public interface ITransaction {

  public Transaction getNative();

  public long getAmountNQT();

  public long getFeeNQT();

  public String getStringId();

  public IAccount getSender();

  public IAccount getReceiver();

  public Date getTimestamp();

  public long getAmountReceivedNQT(Long accountId);

  public long getAmountSpendNQT(Long accountId);

  public long getFeePaidNQT(Long accountId);

  public long getRunningTotalNQT();

  public int getNumberOfConfirmations();

}
