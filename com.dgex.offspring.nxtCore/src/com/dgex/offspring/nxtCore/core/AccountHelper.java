package com.dgex.offspring.nxtCore.core;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import nxt.Account;
import nxt.Alias;
import nxt.Asset;
import nxt.Attachment;
import nxt.Attachment.MessagingArbitraryMessage;
import nxt.Block;
import nxt.Generator;
import nxt.Nxt;
import nxt.Transaction;
import nxt.TransactionType;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import nxt.util.DbIterator;

import org.apache.log4j.Logger;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.IAlias;
import com.dgex.offspring.nxtCore.service.IAsset;
import com.dgex.offspring.nxtCore.service.IMessage;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.ITransaction;

public class AccountHelper implements IAccount {

  static Logger logger = Logger.getLogger(AccountHelper.class);

  private final String privateKey;
  private final Long id;
  private Account account = null;
  private byte[] publicKey = null;
  private final INxtService nxt;
  private final static List<Transaction> EMPTY_TRANSACTIONS = new ArrayList<Transaction>();

  public AccountHelper(INxtService nxt, Long id) {
    this(nxt, id, null);
  }

  public AccountHelper(INxtService nxt, Long id, String privateKey) {
    this.nxt = nxt;
    this.id = id;
    this.privateKey = privateKey;
  }

  @Override
  public Account getNative() {
    if (account != null)
      return account;

    return (account = Account.getAccount(id));
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof AccountHelper) {
      return ((AccountHelper) other).getId().equals(getId());
    }
    return false;
  }

  @Override
  public String getStringId() {
    return Convert.toUnsignedLong(getId());
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public String getPrivateKey() {
    return privateKey;
  }

  @Override
  public byte[] getPublicKey() {
    if (publicKey != null)
      return publicKey;

    Account a = getNative();
    if (a != null)
      return (publicKey = a.getPublicKey());

    if (privateKey != null)
      return (publicKey = Crypto.getPublicKey(privateKey));

    return null;
  }

  @Override
  public long getBalance() {
    Account a = getNative();
    if (a == null)
      return 0l;

    return (a.getGuaranteedBalance(42) / 100L);
  }

  @Override
  public long getUnconfirmedBalance() {
    Account a = getNative();
    if (a == null)
      return 0l;

    return a.getUnconfirmedBalance() / 100L;
  }

  @Override
  public long getAssetBalance(Long assetId) {
    Account a = getNative();
    if (a == null)
      return 0l;

    Integer balance = a.getAssetBalances().get(assetId);
    if (balance == null)
      return 0l;

    return balance.longValue();
  }

  @Override
  public long getUnconfirmedAssetBalance(Long assetId) {
    Account a = getNative();
    if (a == null)
      return 0l;

    Integer balance = a.getUnconfirmedAssetBalance(assetId);
    if (balance == null)
      return 0l;

    return balance.longValue();
  }

  @Override
  public List<IMessage> getMessages() {
    List<IMessage> messages = new ArrayList<IMessage>();
    for (Transaction t : getNativeTransactions()) {
      if (TransactionType.Messaging.ARBITRARY_MESSAGE.equals(t.getType())) {
        Attachment.MessagingArbitraryMessage msg = (MessagingArbitraryMessage) t
            .getAttachment();

        String text;
        try {
          text = new String(msg.getMessage(), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
          text = "UTF-8 not supported";
        }

        IMessage message = new MessageHelper(new AccountHelper(nxt,
            t.getSenderId()), new AccountHelper(nxt, t.getRecipientId()), text,
            t.getTimestamp(), t.getId());
        messages.add(message);
      }
    }
    return messages;
  }

  @Override
  public List<ITransaction> getTransactions() {
    logger.warn("getTransactions()");

    // StringBuilder sb = new StringBuilder();
    // Account account = getNative();
    List<ITransaction> list = new ArrayList<ITransaction>();
    // long total = 0;
    for (Transaction t : getNativeTransactions()) {
      TransactionHelper transaction = new TransactionHelper(nxt, t);

      // sb.append("----------------------------------------------").append("\n");
      // sb.append(transaction).append("\n");
      // sb.append(" #").append(getStringId());
      // sb.append(" spend=").append(transaction.getAmountSpend(account));
      // sb.append(" received=").append(transaction.getAmountReceived(account))
      // .append("\n");
      // sb.append(" sender=").append(Convert.toUnsignedLong(t.getSenderId()))
      // .append("\n");
      // sb.append(" recipient=")
      // .append(Convert.toUnsignedLong(t.getRecipientId())).append("\n");
      // sb.append(" type=").append(t.getType().getSubtype()).append("\n");
      // sb.append(" subtype=").append(t.getType().getType()).append("\n");
      //
      // total += transaction.getAmountReceived(account);
      // total -= transaction.getAmountSpend(account);
      //
      // sb.append("Total=").append(total).append("\n");
      //
      // transaction.setRunningTotal(total);
      list.add(transaction);
    }
    // logger.info("---------------------\n" + sb.toString());
    return list;
  }

  @Override
  public List<Transaction> getNativeTransactions() {
    logger.warn("getNativeTransactions()");

    Account a = getNative();
    if (a != null) {
      return nxt.getDB().getBalanceAlteringTransactionsForAccount(a);
    }
    return EMPTY_TRANSACTIONS;
  }

  @Override
  public List<Block> getForgedBlocks() {
    logger.warn("getForgedBlocks()");

    List<Block> result = new ArrayList<Block>();
    Account a = getNative();
    if (a != null) {
      DbIterator<? extends Block> iter = Nxt.getBlockchain().getBlocks(a, 0);
      while (iter.hasNext()) {
        result.add(iter.next());
      }
    }
    return result;
  }

  @Override
  public int getForgedFee() {
    int fee = 0;
    Account a = getNative();
    if (a != null) {
      DbIterator<? extends Block> iter = Nxt.getBlockchain().getBlocks(a, 0);
      while (iter.hasNext()) {
        fee += iter.next().getTotalFee();
      }
    }
    return fee;
  }

  @Override
  public boolean startForging() {
    if (privateKey != null) {
      if (isForging()) {
        logger.warn("startForging: " + getStringId() + " is already forging");
        return true;
      }
      else {
        Generator generator = Generator.startForging(privateKey);
        if (generator == null) {
          logger.warn("startForging: " + getStringId()
              + " could not obtain Generator");
        }
        else {
          nxt.getEventBroker().post(INxtService.TOPIC_START_FORGING, generator);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean stopForging() {
    if (privateKey != null) {
      if (!isForging()) {
        logger.warn("stopForging: " + getStringId() + " is not forging");
        return true;
      }
      else {
        Generator generator = Generator.stopForging(privateKey);
        if (generator == null) {
          logger.warn("stopForging: " + getStringId()
              + " could not obtain Generator");
        }
        else {
          nxt.getEventBroker().post(INxtService.TOPIC_STOP_FORGING, generator);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isForging() {
    if (privateKey != null) {
      return Generator.getGenerator(privateKey) != null;
    }
    return false;
  }

  @Override
  public List<IAlias> getAliases() {
    List<IAlias> result = new ArrayList<IAlias>();
    List<Alias> natives = getNativeAliases();
    for (Alias alias : natives) {
      result.add(new AliasHelper(alias));
    }
    List<IAlias> remove = new ArrayList<IAlias>();
    for (IAlias alias : nxt.getPendingAliases()) {
      if (alias.getAccount().equals(getNative())) {
        if (result.indexOf(alias) != -1) {
          remove.add(alias);
        }
        else {
          result.add(0, alias);
        }
      }
    }
    for (IAlias a : remove) {
      nxt.getPendingAliases().remove(a);
    }
    return result;
  }

  @Override
  public List<Alias> getNativeAliases() {
    List<Alias> result = new ArrayList<Alias>();
    Account a = getNative();
    for (Alias alias : Alias.getAllAliases()) {
      if (alias.getAccount().equals(a)) {
        result.add(alias);
      }
    }
    return result;
  }

  @Override
  public boolean isReadOnly() {
    return privateKey == null;
  }

  @Override
  public List<IAsset> getIssuedAssets() {
    Account account = getNative();
    List<IAsset> result = new ArrayList<IAsset>();
    if (account != null) {
      logger.info("Get issued assets for " + getStringId());
      for (Long assetId : account.getAssetBalances().keySet()) {

        logger.info("Found one " + Convert.toUnsignedLong(assetId));

        Asset asset = Asset.getAsset(assetId);
        result.add(new AssetHelper(asset));
      }

      List<IAsset> remove = new ArrayList<IAsset>();
      for (IAsset asset : nxt.getPendingAssets()) {
        if (asset.getIssuer().equals(getNative())) {
          if (result.indexOf(asset) != -1) {
            remove.add(asset);
          }
          else {
            result.add(0, asset);
          }
        }
      }
      for (IAsset a : remove) {
        nxt.getPendingAssets().remove(a);
      }

      //

      //
      // for (Asset a : Asset.getAllAssets()) {
      // Long id = a.getId();
      // int unconfirmed = account.getUnconfirmedAssetBalance(id);
      // int confirmed = account.getAssetBalances().get(id);
      //
      // logger.info("Asset: " + a.getName() + " "
      // + Convert.toUnsignedLong(a.getAccountId()) + " unconfirmed="
      // + unconfirmed + " confirmed=" + confirmed);
      // }
    }
    return result;
  }

  @Override
  public TransactionHelper.IteratorAsList getUserTransactions() {
    logger.warn("getUserTransactions()");
    Account account = getNative();
    if (account != null) {

      return new TransactionHelper.IteratorAsList(nxt, Nxt.getBlockchain()
          .getTransactions(account, (byte) -1, (byte) -1, 0));
    }
    return null;
  }
}
