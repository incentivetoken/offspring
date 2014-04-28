package com.dgex.offspring.nxtCore.core;

import java.util.ArrayList;
import java.util.List;

import nxt.Account;
import nxt.Alias;
import nxt.Block;
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

    /*
     * Initialize the Peer listeners (this used to cause a crash since Peer.java
     * is not safe to access before Nxt.init() is called)
     */
    listeners.initPeerListeners();

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
      long amountNQT, short deadline, long feeNQT,
      String referencedTransactionFullHash)
      throws nxt.NxtException.ValidationException, TransactionException {

    Transaction transaction = TransactionPayment.create(sender, recipient,
        amountNQT, deadline, feeNQT, referencedTransactionFullHash, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createAssignAliasTransaction(IAccount sender,
      String alias, String uri, short deadline, long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionAssignAlias.create(sender, alias, uri,
        deadline, feeNQT, referencedTransactionFullHash, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createSendMessageTransaction(IAccount sender,
      Long recipient, byte[] message, short deadline, long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionSendMessage.create(sender, recipient,
        message, deadline, feeNQT, referencedTransactionFullHash, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createIssueAssetTransaction(IAccount sender, String name,
      String description, long quantityQNT, byte decimals, short deadline,
      long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionIssueAsset.create(sender, name,
        description, quantityQNT, decimals, deadline, feeNQT,
        referencedTransactionFullHash, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createPlaceAskOrderTransaction(IAccount sender,
      long asset, long quantityQNT, long priceNQT, short deadline, long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionPlaceAskOrder.create(sender, asset,
        quantityQNT, priceNQT, deadline, feeNQT, referencedTransactionFullHash,
        this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createPlaceBidOrderTransaction(IAccount sender,
      long asset, long quantityQNT, long priceNQT, short deadline, long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionPlaceBidOrder.create(sender, asset,
        quantityQNT, priceNQT, deadline, feeNQT, referencedTransactionFullHash,
        this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createCancelAskOrderTransaction(IAccount sender,
      Long order, short deadline, long feeNQT,
      String referencedTransactionFullHash)
      throws ValidationException, TransactionException {

    Transaction transaction = TransactionCancelAskOrder.create(sender, order,
        deadline, feeNQT, referencedTransactionFullHash, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createCancelBidOrderTransaction(IAccount sender,
      Long order, short deadline, long feeNQT,
      String referencedTransactionFullHash)
      throws ValidationException, TransactionException {

    Transaction transaction = TransactionCancelBidOrder.create(sender, order,
        deadline, feeNQT, referencedTransactionFullHash, this);

    broker.post(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION,
        new TransactionHelper(this, transaction));

    return transaction;
  }

  @Override
  public Transaction createTransferAssetTransaction(IAccount sender,
      Long recipient, long asset, long quantityQNT, String comment,
      short deadline,
      long feeNQT, String referencedTransactionFullHash)
      throws ValidationException,
      TransactionException {

    Transaction transaction = TransactionTransferAsset.create(sender,
        recipient, asset, quantityQNT, comment, deadline, feeNQT,
        referencedTransactionFullHash, this);

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
  public Long getBalanceForAccountNQT(String id) {
    try {
      Account account = Account.getAccount(Convert.parseUnsignedLong(id));
      return account.getBalanceNQT();
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
    return Nxt.getBlockchain().getHeight();
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

}
