package com.dgex.offspring.nxtCore.core;

import java.util.ArrayList;
import java.util.List;

import nxt.Account;
import nxt.Alias;
import nxt.Block;
import nxt.BlockchainProcessor;
import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Token;
import nxt.Trade;
import nxt.Transaction;
import nxt.crypto.Crypto;
import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Convert;
import nxt.util.Listener;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.dgex.offspring.config.Config;
import com.dgex.offspring.nxtCore.h2.H2ListenerHelper;
import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.IAlias;
import com.dgex.offspring.nxtCore.service.IAsset;
import com.dgex.offspring.nxtCore.service.INxtDB;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.IPeer;
import com.dgex.offspring.nxtCore.service.ITransaction;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class NXTService implements INxtService {

  private static Logger logger = Logger.getLogger(NXTService.class);
  public final List<IAccount> unlockedAccounts = new ArrayList<IAccount>();

  private NXTListeners listeners;
  private boolean initializing = false;
  private boolean scanning = false;
  private IEventBroker broker;
  private final INxtDB db = new NxtDB(this);

  private final List<Transaction> pendingTransactions = new ArrayList<Transaction>();
  private final List<IAlias> pendingAliases = new ArrayList<IAlias>();
  private final List<IAsset> pendingAssets = new ArrayList<IAsset>();

  @Override
  public void initialize(IEventBroker _broker, final UISynchronize sync) {
    this.broker = _broker;

    logger.info("Initialize START");
    listeners = new NXTListeners(broker, this);

    H2ListenerHelper.getInstance().initialize(broker);
    initializing = true;

    /* Start the initialization process */
    broker.post(INxtService.TOPIC_INITIALIZATION_START, 1);

    /* The first block scanned signals reading of database is complete */
    broker.subscribe(INxtService.TOPIC_BLOCK_SCANNED, new EventHandler() {

      @Override
      public void handleEvent(Event event) {
        broker.unsubscribe(this);
        setScanning(true);
        broker.post(INxtService.TOPIC_BLOCK_SCANNER_START, 1);
      }
    });

    logger.info("NXT init START");

    try {
      /* Read the database file AND iterate over all records in database */
      Nxt.init(Config.properties);
    }
    catch (final Throwable t) {
      sync.syncExec(new Runnable() {

        @Override
        public void run() {
          MessageDialog
              .openError(
                  Display.getDefault().getActiveShell(),
                  "NXT initialization Error",
                  "A fatal error occured initializing NXT.\n"
                      + "If this keeps occuring consider to delete NXT database folder.\n\n"
                      + t.toString());
        }
      });
      System.exit(-1);
      return;
    }

    logger.info("NXT init END");

    broker.post(INxtService.TOPIC_BLOCK_SCANNER_FINISHED, getBlockCount());

    broker.post(INxtService.TOPIC_INITIALIZATION_FINISHED, 1);
    setScanning(false);

    initializing = false;
    logger.info("Initialization COMPLETE");

    /* Nxt initialized and ready turn on forging for all accounts */
    sync.asyncExec(new Runnable() {

      @Override
      public void run() {
        for (IAccount account : unlockedAccounts) {
          account.startForging();
        }
      }
    });
  }

  @Override
  public IEventBroker getEventBroker() {
    return broker;
  }

  @Override
  public void shutdown() {
    Nxt.shutdown();
  }

  @Override
  public void fullReset() {
    Nxt.getBlockchainProcessor().fullReset();
  }

  @Override
  public INxtDB getDB() {
    return db;
  }

  @Override
  public boolean isInitializing() {
    return initializing;
  }

  @Override
  public boolean isScanning() {
    return scanning;
  }

  @Override
  public void setScanning(boolean scanning) {
    this.scanning = scanning;
  }

  @Override
  public List<Transaction> getPendingTransactions() {
    return pendingTransactions;
  }

  @Override
  public List<IAlias> getPendingAliases() {
    return pendingAliases;
  }

  @Override
  public List<IAsset> getPendingAssets() {
    return pendingAssets;
  }

  @Override
  public ITransaction wrap(Transaction transaction) {
    return new TransactionHelper(this, transaction);
  }

  @Override
  public IAccount wrap(Account account) {
    return new AccountHelper(this, account.getId());
  }

  @Override
  public IPeer wrap(Peer peer) {
    return new PeerHelper(peer);
  }

  private IAccount getUnlockedAccount(String passphrase) {
    for (IAccount a : unlockedAccounts) {
      if (a.getPrivateKey().equals(passphrase))
        return a;
    }
    return null;
  }

  @Override
  public IAccount unlock(String passphrase) {
    IAccount account = getUnlockedAccount(passphrase);
    if (account == null) {
      byte[] publicKey = Crypto.getPublicKey(passphrase);
      Long id = Account.getId(publicKey);
      account = new AccountHelper(this, id, passphrase);
      unlockedAccounts.add(account);
    }
    return account;
  }

  @Override
  public List<IAccount> getUnlockedAccounts() {
    return unlockedAccounts;
  }

  @Override
  public List<Peer> getAllPeers() {
    return new ArrayList<Peer>(Peers.getAllPeers());
  }

  @Override
  public List<Peer> getAllBlacklistedPeers() {
    List<Peer> blacklisted = new ArrayList<Peer>();
    for (Peer p : Peers.getAllPeers()) {
      if (p.isBlacklisted())
        blacklisted.add(p);
    }
    return blacklisted;
  }

  @Override
  public List<Peer> getAllConnectedPeers() {
    List<Peer> connected = new ArrayList<Peer>();
    for (Peer p : Peers.getAllPeers()) {
      if (Peer.State.CONNECTED.equals(p.getState()))
        connected.add(p);
    }
    return connected;
  }

  @Override
  public List<Peer> getAllWellknownPeers() {
    List<Peer> wellknown = new ArrayList<Peer>();
    for (Peer p : Peers.getAllPeers()) {
      if (p.isWellKnown())
        wellknown.add(p);
    }
    return wellknown;
  }

  @Override
  public Transaction createPaymentTransaction(IAccount sender, Long recipient,
      int amount, short deadline, int fee, Long referencedTransaction)
      throws nxt.NxtException.ValidationException, TransactionException {

    Transaction transaction = TransactionPayment.create(sender, recipient,
        amount, deadline, fee, referencedTransaction, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createAssignAliasTransaction(IAccount sender,
      String alias, String uri, short deadline, int fee,
      Long referencedTransaction) throws ValidationException,
      TransactionException {

    return TransactionAssignAlias.create(sender, alias, uri, deadline, fee,
        referencedTransaction, this);
  }

  @Override
  public Transaction createSendMessageTransaction(IAccount sender,
      IAccount _recipient, byte[] message, short deadline, int fee,
      Long referencedTransaction) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionSendMessage.create(sender, _recipient,
        message, deadline, fee, referencedTransaction, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createIssueAssetTransaction(IAccount sender, String name,
      String description, int quantity, short deadline, int fee,
      Long referencedTransaction) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionIssueAsset.create(sender, name,
        description, quantity, deadline, fee, referencedTransaction, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createPlaceAskOrderTransaction(IAccount sender,
      long asset, int quantity, long price, short deadline, int fee,
      Long referencedTransaction) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionPlaceAskOrder.create(sender, asset,
        quantity, price, deadline, fee, referencedTransaction, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createPlaceBidOrderTransaction(IAccount sender,
      long asset, int quantity, long price, short deadline, int fee,
      Long referencedTransaction) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionPlaceBidOrder.create(sender, asset,
        quantity, price, deadline, fee, referencedTransaction, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createCancelAskOrderTransaction(IAccount sender,
      Long order, short deadline, int fee, Long referencedTransaction)
      throws ValidationException, TransactionException {

    Transaction transaction = TransactionCancelAskOrder.create(sender, order,
        deadline, fee, referencedTransaction, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createCancelBidOrderTransaction(IAccount sender,
      Long order, short deadline, int fee, Long referencedTransaction)
      throws ValidationException, TransactionException {

    Transaction transaction = TransactionCancelBidOrder.create(sender, order,
        deadline, fee, referencedTransaction, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createTransferAssetTransaction(IAccount sender,
      Long recipient, long asset, int quantity, short deadline, int fee,
      Long referencedTransaction) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionTransferAsset.create(sender,
        recipient, asset, quantity, deadline, fee, referencedTransaction, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public String getAccountForPrivateKey(String privateKey) {
    byte[] publicKey = Crypto.getPublicKey(privateKey);
    return Convert.toUnsignedLong(Account.getId(publicKey));
  }

  @Override
  public Long getBalanceForAccount(String id) {
    try {
      Account account = Account.getAccount(Convert.parseUnsignedLong(id));
      return account.getBalance();
    }
    catch (RuntimeException e) {}
    return null;
  }

  @Override
  public String generateAuthorizationToken(String privateKey, String website) {
    return Token.generateToken(privateKey, website.trim());
  }

  @Override
  public Token getToken(String website, String token) {
    return Token.parseToken(token, website.trim());
  }

  @Override
  public String getSoftwareVersion() {
    return Nxt.VERSION;
  }

  @Override
  public int getBlockCount() {
    return Nxt.getBlockchain().getBlockCount();
  }

  @Override
  public Block getBlockAtHeight(int height) {
    try {
      long id = Nxt.getBlockchain().getBlockIdAtHeight(height);
      return Nxt.getBlockchain().getBlock(id);
    }
    catch (Exception ex) {}
    return null;
  }

  @Override
  public long convertTimestamp(long timestamp) {
    return ((timestamp * 1000) + Constants.EPOCH_BEGINNING - 500L);
  }

  @Override
  public void broacastTransaction(Transaction transaction)
      throws ValidationException {
    pendingTransactions.add(transaction);
    Nxt.getTransactionProcessor().broadcast(transaction);

    // Long assetId = transaction.getId();
    // IAsset asset = new AssetHelper(user.getAccount().getId(), assetId,
    // name, description, quantity);
    // nxt.getPendingAssets().add(asset);
    // assetsViewer.refresh();
  }

  @Override
  public boolean aliasIsRegistered(String str, IAccount account) {
    Alias alias = Alias.getAlias(str);
    if (alias != null) {
      return !alias.getAccount().equals(account.getNative());
    }
    return false;
  }

  @Override
  public String getAlias(String alias) {
    Alias a = Alias.getAlias(alias);
    if (a instanceof Alias) {
      return a.getURI();
    }
    return null;
  }

  @Override
  public List<Trade> getTrades(Long assetId) {
    try {
      return Trade.getTrades(assetId);
    }
    catch (NullPointerException ex) {
      return new ArrayList<Trade>();
    }
  }

  private int smartBlockCount = -1;
  private int smartTransactionCount = -1;
  private final boolean blockListenersRegistered = false;

  private void registerBlockListener() {
    if (blockListenersRegistered)
      return;

    Nxt.getBlockchainProcessor().addListener(new Listener<Block>() {

      @Override
      public void notify(Block block) {
        smartBlockCount++;
        smartTransactionCount += block.getTransactionIds().size();
      }
    }, BlockchainProcessor.Event.BLOCK_PUSHED);

    Nxt.getBlockchainProcessor().addListener(new Listener<Block>() {

      @Override
      public void notify(Block block) {
        smartBlockCount--;
        smartTransactionCount -= block.getTransactionIds().size();
      }
    }, BlockchainProcessor.Event.BLOCK_POPPED);
  }

  @Override
  public int getSmartBlockCount() {
    if (smartBlockCount == -1) {
      smartBlockCount = Nxt.getBlockchain().getBlockCount();
      registerBlockListener();
    }
    return smartBlockCount;
  }

  @Override
  public int getSmartTransactionCount() {
    if (smartTransactionCount == -1) {
      smartTransactionCount = Nxt.getBlockchain().getTransactionCount();
      registerBlockListener();
    }
    return smartTransactionCount;
  }

}
