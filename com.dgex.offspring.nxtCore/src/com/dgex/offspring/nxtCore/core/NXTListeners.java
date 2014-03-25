package com.dgex.offspring.nxtCore.core;

import java.util.List;

import nxt.Account;
import nxt.Block;
import nxt.BlockchainProcessor;
import nxt.Generator;
import nxt.Nxt;
import nxt.Transaction;
import nxt.TransactionProcessor;
import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Listener;
import nxt.util.Logger;

import org.eclipse.e4.core.services.events.IEventBroker;

import com.dgex.offspring.nxtCore.service.INxtService;

public class NXTListeners {

  private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
      .getLogger(NXTListeners.class);

  private final Listener<List<Transaction>> addConfirmedTransaction;
  private final Listener<List<Transaction>> addUnconfirmedTransaction;
  private final Listener<List<Transaction>> removeUnconfirmedTransaction;
  private final Listener<List<Transaction>> addDoubleSpendingTransaction;
  private final Listener<Block> blockPopped;
  private final Listener<Block> blockPushed;
  private final Listener<Block> blockGenerated;
  private final Listener<Block> blockScanned;
  private final Listener<Block> blockRescanBegin;
  private final Listener<Block> blockRescanEnd;
  private final Listener<Account> accountUpdateBalance;
  private final Listener<Account> accountUpdateUnconfirmedBalance;
  private final Listener<Peer> addActivePeer;
  private final Listener<Peer> blacklistPeer;
  private final Listener<Peer> changedActivePeer;
  private final Listener<Peer> deactivatePeer;
  private final Listener<Peer> updateDownloadedVolume;
  private final Listener<Peer> peerRemove;
  private final Listener<Peer> unblacklistPeer;
  private final Listener<Peer> updateUploadedVolume;
  private final Listener<Peer> peerUpdateWeight;

  private final Listener<Exception> logException;
  private final Listener<String> logMessage;

  private final Listener<Generator> generatorDeadline;

  public NXTListeners(final IEventBroker broker, final INxtService nxt) {

    generatorDeadline = new Listener<Generator>() {

      @Override
      public void notify(Generator generator) {
        broker.post(INxtService.TOPIC_GENERATION_DEADLINE, generator);
      }
    };

    addConfirmedTransaction = new Listener<List<Transaction>>() {

      @Override
      public void notify(List<Transaction> list) {
        for (Transaction t : list) {
          // logger.info("add confirmed transaction " + t);
          boolean filtered = Utils.filterTransaction(nxt, t.getSenderId(),
              t.getRecipientId());
          if (filtered) {
            broker.post(INxtService.TOPIC_ADD_FILTERED_TRANSACTION,
                new TransactionHelper(nxt, t));
          }
          // else {
          // broker.post(INxtService.TOPIC_ADD_TRANSACTION,
          // new TransactionHelper(nxt, t));
          // }
        }
      }
    };

    addUnconfirmedTransaction = new Listener<List<Transaction>>() {

      @Override
      public void notify(List<Transaction> list) {
        for (Transaction t : list) {
          // logger.info("add unconfirmed transaction " + t);
          boolean filtered = Utils.filterTransaction(nxt, t.getSenderId(),
              t.getRecipientId());
          if (filtered) {
            broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
                new TransactionHelper(nxt, t));
          }
          // else {
          // broker.post(INxtService.TOPIC_ADD_UNCONFIRMED_TRANSACTION,
          // new TransactionHelper(nxt, t));
          // }
        }
      }
    };

    removeUnconfirmedTransaction = new Listener<List<Transaction>>() {

      @Override
      public void notify(List<Transaction> list) {
        for (Transaction t : list) {
          // logger.info("remove unconfirmed transaction " + t);
          boolean filtered = Utils.filterTransaction(nxt, t.getSenderId(),
              t.getRecipientId());
          if (filtered) {
            broker.post(
                INxtService.TOPIC_REMOVE_FILTERED_UNCONFIRMED_TRANSACTION,
                new TransactionHelper(nxt, t));
          }
          // else {
          // broker.post(INxtService.TOPIC_REMOVE_UNCONFIRMED_TRANSACTION,
          // new TransactionHelper(nxt, t));
          // }
        }
      }
    };

    addDoubleSpendingTransaction = new Listener<List<Transaction>>() {

      @Override
      public void notify(List<Transaction> list) {
        for (Transaction t : list) {
          // logger.info("add double spending transaction " + t);
          broker.post(INxtService.TOPIC_ADD_DOUBLESPENDING_TRANSACTION,
              new TransactionHelper(nxt, t));
        }
      }
    };

    blockPopped = new Listener<Block>() {

      @Override
      public void notify(Block b) {
        // logger.info("block popped " + b);
        broker.post(INxtService.TOPIC_BLOCK_POPPED, b);
      }
    };

    blockPushed = new Listener<Block>() {

      @Override
      public void notify(Block b) {
        // logger.info("block pushed " + b);
        broker.post(INxtService.TOPIC_BLOCK_PUSHED, b);
      }
    };

    blockScanned = new Listener<Block>() {

      long lastBlockScanned = 0;
      long rate = 1000 / 20;

      @Override
      public void notify(Block b) {
        long time = System.currentTimeMillis();
        if ((time - lastBlockScanned) > rate) {
          lastBlockScanned = time;
          broker.post(INxtService.TOPIC_BLOCK_SCANNED, b);
        }
      }
    };

    blockGenerated = new Listener<Block>() {

      @Override
      public void notify(Block b) {
        // logger.info("block generated " + b);
        broker.post(INxtService.TOPIC_BLOCK_GENERATED, b);
      }
    };

    blockRescanBegin = new Listener<Block>() {

      @Override
      public void notify(Block b) {
        // logger.info("block generated " + b);
        nxt.setScanning(true);
        broker.post(INxtService.TOPIC_BLOCK_SCANNER_START, b);
      }
    };

    blockRescanEnd = new Listener<Block>() {

      @Override
      public void notify(Block b) {
        // logger.info("block generated " + b);
        broker.post(INxtService.TOPIC_BLOCK_SCANNER_FINISHED, b);
        nxt.setScanning(false);
      }
    };

    accountUpdateBalance = new Listener<Account>() {

      @Override
      public void notify(Account account) {
        // logger.info("account update balance " + account);
        if (account != null && Utils.filterAccount(nxt, account.getId())) {
          broker.post(INxtService.TOPIC_ACCOUNT_UPDATE_BALANCE,
              new AccountHelper(nxt, account.getId()));
        }
      }
    };

    accountUpdateUnconfirmedBalance = new Listener<Account>() {

      @Override
      public void notify(Account account) {
        // logger.info("account update unconfirmed balance " + account);
        if (account != null && Utils.filterAccount(nxt, account.getId())) {
          broker.post(INxtService.TOPIC_ACCOUNT_UPDATE_UNCONFIRMED_BALANCE,
              new AccountHelper(nxt, account.getId()));
        }
      }
    };

    addActivePeer = new Listener<Peer>() {

      @Override
      public void notify(Peer peer) {
        // logger.info("add active peer " + peer);
        broker.post(INxtService.TOPIC_ADDED_ACTIVE_PEER, peer);
      }
    };

    blacklistPeer = new Listener<Peer>() {

      @Override
      public void notify(Peer peer) {
        // logger.info("blacklist peer " + peer);
        broker.post(INxtService.TOPIC_BLACKLIST_PEER, peer);
      }
    };

    changedActivePeer = new Listener<Peer>() {

      @Override
      public void notify(Peer peer) {
        // logger.info("changed active peer " + peer);
        broker.post(INxtService.TOPIC_CHANGED_ACTIVE_PEER, peer);
      }
    };

    deactivatePeer = new Listener<Peer>() {

      @Override
      public void notify(Peer peer) {
        logger.info("deactive peer " + peer);
        broker.post(INxtService.TOPIC_DEACTIVATE_PEER, peer);
      }
    };

    updateDownloadedVolume = new Listener<Peer>() {

      @Override
      public void notify(Peer peer) {
        // logger.info("update download volume peer " + peer);
        broker.post(INxtService.TOPIC_DOWNLOADED_VOLUME_PEER, peer);
      }
    };

    peerRemove = new Listener<Peer>() {

      @Override
      public void notify(Peer peer) {
        // logger.info("remove peer " + peer);
        broker.post(INxtService.TOPIC_REMOVE_PEER, peer);
      }
    };

    unblacklistPeer = new Listener<Peer>() {

      @Override
      public void notify(Peer peer) {
        // logger.info("unblacklist peer " + peer);
        broker.post(INxtService.TOPIC_UNBLACKLIST_PEER, peer);
      }
    };

    updateUploadedVolume = new Listener<Peer>() {

      @Override
      public void notify(Peer peer) {
        // logger.info("update uploaded volume peer " + peer);
        broker.post(INxtService.TOPIC_UPLOADED_VOLUME_PEER, peer);
      }
    };

    peerUpdateWeight = new Listener<Peer>() {

      @Override
      public void notify(Peer peer) {
        // logger.info("update weight peer " + peer);
        broker.post(INxtService.TOPIC_WEIGHT_PEER, peer);
      }
    };

    logException = new Listener<Exception>() {

      @Override
      public void notify(Exception exception) {
        logger.trace("Nxt.Logger.logException: " + exception);
        broker.post(INxtService.TOPIC_LOGGER_EXCEPTION, exception);
      }
    };

    logMessage = new Listener<String>() {

      @Override
      public void notify(String message) {
        logger.trace("Nxt.Logger.logMessage: " + message);
        broker.post(INxtService.TOPIC_LOGGER_MESSAGE, message);
      }
    };

    Nxt.getTransactionProcessor().addListener(addConfirmedTransaction,
        TransactionProcessor.Event.ADDED_CONFIRMED_TRANSACTIONS);
    Nxt.getTransactionProcessor().addListener(addUnconfirmedTransaction,
        TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);
    Nxt.getTransactionProcessor().addListener(removeUnconfirmedTransaction,
        TransactionProcessor.Event.REMOVED_UNCONFIRMED_TRANSACTIONS);
    Nxt.getTransactionProcessor().addListener(addDoubleSpendingTransaction,
        TransactionProcessor.Event.ADDED_DOUBLESPENDING_TRANSACTIONS);

    Nxt.getBlockchainProcessor().addListener(blockPopped,
        BlockchainProcessor.Event.BLOCK_POPPED);
    Nxt.getBlockchainProcessor().addListener(blockPushed,
        BlockchainProcessor.Event.BLOCK_PUSHED);
    Nxt.getBlockchainProcessor().addListener(blockScanned,
        BlockchainProcessor.Event.BLOCK_SCANNED);
    Nxt.getBlockchainProcessor().addListener(blockGenerated,
        BlockchainProcessor.Event.BLOCK_GENERATED);

    Nxt.getBlockchainProcessor().addListener(blockRescanBegin,
        BlockchainProcessor.Event.RESCAN_BEGIN);
    Nxt.getBlockchainProcessor().addListener(blockRescanEnd,
        BlockchainProcessor.Event.RESCAN_END);

    Generator.addListener(generatorDeadline,
        Generator.Event.GENERATION_DEADLINE);

    Account.addListener(accountUpdateBalance, Account.Event.BALANCE);
    Account.addListener(accountUpdateUnconfirmedBalance,
        Account.Event.UNCONFIRMED_BALANCE);

    Peers.addListener(addActivePeer, Peers.Event.ADDED_ACTIVE_PEER);
    Peers.addListener(blacklistPeer, Peers.Event.BLACKLIST);
    Peers.addListener(changedActivePeer, Peers.Event.CHANGED_ACTIVE_PEER);
    Peers.addListener(deactivatePeer, Peers.Event.DEACTIVATE);
    Peers.addListener(updateDownloadedVolume, Peers.Event.DOWNLOADED_VOLUME);
    Peers.addListener(peerRemove, Peers.Event.REMOVE);
    Peers.addListener(unblacklistPeer, Peers.Event.UNBLACKLIST);
    Peers.addListener(updateUploadedVolume, Peers.Event.UPLOADED_VOLUME);
    Peers.addListener(peerUpdateWeight, Peers.Event.WEIGHT);

    Logger.addExceptionListener(logException, Logger.Event.EXCEPTION);
    Logger.addMessageListener(logMessage, Logger.Event.MESSAGE);
  }

  public void dispose() {

    /* This is currently never called */

    Nxt.getTransactionProcessor().removeListener(addConfirmedTransaction,
        TransactionProcessor.Event.ADDED_CONFIRMED_TRANSACTIONS);
    Nxt.getTransactionProcessor().removeListener(addUnconfirmedTransaction,
        TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);
    Nxt.getTransactionProcessor().removeListener(removeUnconfirmedTransaction,
        TransactionProcessor.Event.REMOVED_UNCONFIRMED_TRANSACTIONS);
    Nxt.getTransactionProcessor().removeListener(addDoubleSpendingTransaction,
        TransactionProcessor.Event.ADDED_DOUBLESPENDING_TRANSACTIONS);

    Nxt.getBlockchainProcessor().removeListener(blockPopped,
        BlockchainProcessor.Event.BLOCK_POPPED);
    Nxt.getBlockchainProcessor().removeListener(blockPushed,
        BlockchainProcessor.Event.BLOCK_PUSHED);
    Nxt.getBlockchainProcessor().removeListener(blockScanned,
        BlockchainProcessor.Event.BLOCK_SCANNED);
    Nxt.getBlockchainProcessor().removeListener(blockGenerated,
        BlockchainProcessor.Event.BLOCK_GENERATED);

    Account.removeListener(accountUpdateBalance, Account.Event.BALANCE);
    Account.removeListener(accountUpdateUnconfirmedBalance,
        Account.Event.UNCONFIRMED_BALANCE);

    Peers.removeListener(addActivePeer, Peers.Event.ADDED_ACTIVE_PEER);
    Peers.removeListener(blacklistPeer, Peers.Event.BLACKLIST);
    Peers.removeListener(changedActivePeer, Peers.Event.CHANGED_ACTIVE_PEER);
    Peers.removeListener(deactivatePeer, Peers.Event.DEACTIVATE);
    Peers.removeListener(updateDownloadedVolume, Peers.Event.DOWNLOADED_VOLUME);
    Peers.removeListener(peerRemove, Peers.Event.REMOVE);
    Peers.removeListener(unblacklistPeer, Peers.Event.UNBLACKLIST);
    Peers.removeListener(updateUploadedVolume, Peers.Event.UPLOADED_VOLUME);
    Peers.removeListener(peerUpdateWeight, Peers.Event.WEIGHT);

    Logger.addExceptionListener(logException, Logger.Event.EXCEPTION);
    Logger.addMessageListener(logMessage, Logger.Event.MESSAGE);

  }
}
