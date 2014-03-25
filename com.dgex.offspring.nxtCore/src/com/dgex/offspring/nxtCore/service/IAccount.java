package com.dgex.offspring.nxtCore.service;

import java.util.List;

import nxt.Account;
import nxt.Alias;
import nxt.Block;
import nxt.Transaction;

import com.dgex.offspring.nxtCore.core.TransactionHelper.IteratorAsList;

public interface IAccount {

  public Account getNative();

  public Long getId();

  public String getStringId();

  public long getBalance();

  public long getUnconfirmedBalance();

  public long getAssetBalance(Long assetId);

  public long getUnconfirmedAssetBalance(Long assetId);

  public String getPrivateKey();

  public byte[] getPublicKey();

  public List<ITransaction> getTransactions();

  public List<Transaction> getNativeTransactions();

  public List<Alias> getNativeAliases();

  public List<Block> getForgedBlocks();

  public List<IAlias> getAliases();

  public List<IMessage> getMessages();

  public List<IAsset> getIssuedAssets();

  public int getForgedFee();

  public boolean startForging();

  public boolean stopForging();

  public boolean isForging();

  public boolean isReadOnly();

  IteratorAsList getUserTransactions();

}
