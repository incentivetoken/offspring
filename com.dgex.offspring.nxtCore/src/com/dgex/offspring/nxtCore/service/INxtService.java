package com.dgex.offspring.nxtCore.service;

import java.util.List;

import nxt.Account;
import nxt.Block;
import nxt.NxtException.ValidationException;
import nxt.Token;
import nxt.Trade;
import nxt.Transaction;
import nxt.peer.Peer;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;

public interface INxtService {

  public static String TOPIC_TRANSACTION = "NXT/TRANSACTION/*";
  public static String TOPIC_ADD_TRANSACTION = "NXT/TRANSACTION/ADD_TRANSACTION";
  public static String TOPIC_ADD_UNCONFIRMED_TRANSACTION = "NXT/TRANSACTION/ADD_UNCONFIRMED_TRANSACTION";
  public static String TOPIC_REMOVE_UNCONFIRMED_TRANSACTION = "NXT/TRANSACTION/REMOVE_UNCONFIRMED_TRANSACTION";
  public static String TOPIC_ADD_DOUBLESPENDING_TRANSACTION = "NXT/TRANSACTION/ADD_DOUBLESPENDING_TRANSACTION";

  public static String TOPIC_FILTERED_TRANSACTION = "NXT/FILTERED_TRANSACTION/*";
  public static String TOPIC_ADD_FILTERED_TRANSACTION = "NXT/FILTERED_TRANSACTION/ADD_FILTERED_TRANSACTION";
  public static String TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION = "NXT/FILTERED_TRANSACTION/ADD_FILTERED_UNCONFIRMED_TRANSACTION";
  public static String TOPIC_REMOVE_FILTERED_UNCONFIRMED_TRANSACTION = "NXT/FILTERED_TRANSACTION/REMOVE_FILTERED_UNCONFIRMED_TRANSACTION";

  public static String TOPIC_BLOCK = "NXT/BLOCK/*";
  public static String TOPIC_BLOCK_PUSHED = "NXT/BLOCK/BLOCK_PUSHED";
  public static String TOPIC_BLOCK_POPPED = "NXT/BLOCK/BLOCK_POPPED";
  public static String TOPIC_BLOCK_SCANNED = "NXT/BLOCK/BLOCK_SCANNED";
  public static String TOPIC_BLOCK_GENERATED = "NXT/BLOCK/BLOCK_GENERATED";
  public static String TOPIC_BLOCK_SCANNER_START = "NXT/BLOCK/BLOCK_SCANNER_START";
  public static String TOPIC_BLOCK_SCANNER_FINISHED = "NXT/BLOCK/BLOCK_SCANNER_FINISHED";
  public static String TOPIC_BLOCK_RESCAN_BEGIN = null;
  public static String TOPIC_BLOCK_RESCAN_END = null;

  public static String TOPIC_ACCOUNT_UPDATE_BALANCE = "NXT/ACCOUNT_UPDATE_BALANCE";
  public static String TOPIC_ACCOUNT_UPDATE_UNCONFIRMED_BALANCE = "NXT/ACCOUNT_UPDATE_UNCONFIRMED_BALANCE";

  public static String TOPIC_PEER = "NXT/PEER/*";
  public static String TOPIC_ADDED_ACTIVE_PEER = "NXT/PEER/ADDED_ACTIVE_PEER";
  public static String TOPIC_BLACKLIST_PEER = "NXT/PEER/BLACKLIST_PEER";
  public static String TOPIC_CHANGED_ACTIVE_PEER = "NXT/PEER/CHANGED_ACTIVE_PEER";
  public static String TOPIC_DEACTIVATE_PEER = "NXT/PEER/DEACTIVATE_PEER";
  public static String TOPIC_DOWNLOADED_VOLUME_PEER = "NXT/PEER/DOWNLOADED_VOLUME_PEER";
  public static String TOPIC_REMOVE_PEER = "NXT/PEER/REMOVE_PEER";
  public static String TOPIC_UNBLACKLIST_PEER = "NXT/PEER/UNBLACKLIST_PEER";
  public static String TOPIC_UPLOADED_VOLUME_PEER = "NXT/PEER/UPLOADED_VOLUME_PEER";
  public static String TOPIC_WEIGHT_PEER = "NXT/PEER/WEIGHT_PEER";

  public static String TOPIC_LOGGER = "NXT/LOGGER/*";
  public static String TOPIC_LOGGER_EXCEPTION = "NXT/LOGGER/EXCEPTION";
  public static String TOPIC_LOGGER_MESSAGE = "NXT/LOGGER/MESSAGE";

  public static String TOPIC_INITIALIZATION_START = "NXT/INITIALIZATION/START";
  public static String TOPIC_INITIALIZATION_FINISHED = "NXT/INITIALIZATION/FINISHED";

  public static String TOPIC_GENERATION_DEADLINE = "NXT/GENERATOR/DEADLINE";
  public static String TOPIC_START_FORGING = "NXT/GENERATOR/START_FORGING";
  public static String TOPIC_STOP_FORGING = "NXT/GENERATOR/STOP_FORGING";

  @Deprecated
  public static String TOPIC_SHUTDOWN_START = "NXT/SHUTDOWN/START";

  @Deprecated
  public static String TOPIC_SHUTDOWN_FINISHED = "NXT/SHUTDOWN/FINISHED";

  /**
   * The IEventBroker is not available until later, thats why we pass it here as
   * an argument. The initialize method calls Nxt.init()
   * 
   * @param broker
   * @param sync
   */
  public void initialize(IEventBroker broker, UISynchronize sync);

  /**
   * Returns the IEventBroker
   * 
   * @return
   */
  public IEventBroker getEventBroker();

  /**
   * Performs a full reset of the blockchain. All blocks will be removed from
   * memory and re-read from the database.
   */
  public void fullReset();

  /**
   * Returns true if Nxt.init is currently running (in it's own thread)
   * 
   * @return
   */
  public boolean isInitializing();

  /**
   * Returns true if database is currently being scanned
   * 
   * @return
   */
  public boolean isScanning();

  /**
   * Sets scanning flag
   * 
   * @param scanning
   */
  public void setScanning(boolean scanning);

  /**
   * Returns the INxtDB instance
   * 
   * @return
   */
  public INxtDB getDB();

  /**
   * Broadcasts the transaction
   * 
   * @param transaction
   * @throws ValidationException
   */
  public void broacastTransaction(Transaction transaction)
      throws ValidationException;

  /**
   * Calls shutdown on the Nxt classes, progress is reported through the
   * monitor.
   * 
   * At the start of the shutdown process you can expect TOPIC_SHUTDOWN_START
   * event and once the shutdown has finished expect the TOPIC_SHUTDOWN_FINISHED
   * event.
   * 
   * @param monitor
   */
  public void shutdown();

  /**
   * Wrap as ITransaction
   * 
   * @param transaction
   * @return wrapper
   */
  public ITransaction wrap(Transaction transaction);

  /**
   * Wrap as IAccount
   * 
   * @param account
   * @return wrapper
   */
  public IAccount wrap(Account account);

  /**
   * Wrap as IPeer
   * 
   * @param peer
   * @return wrapper
   */
  public IPeer wrap(Peer peer);

  /**
   * Unlocks the account and turns on forging.
   * 
   * @param passphrase
   * @return IAccount
   */
  public IAccount unlock(String passphrase);

  /**
   * Returns a list of all unlocked accounts
   * 
   * @return list of IAccount
   */
  public List<IAccount> getUnlockedAccounts();

  /**
   * Returns a list of all peers
   * 
   * @return list of Peer
   */
  public List<Peer> getAllPeers();

  /**
   * Returns a list of all blacklisted peers
   * 
   * @return list of Peer
   */
  public List<Peer> getAllBlacklistedPeers();

  /**
   * Returns a list of all connected peers
   * 
   * @return list of Peer
   */
  public List<Peer> getAllConnectedPeers();

  /**
   * Returns a list of all wellknown peers
   * 
   * @return list of Peer
   */
  public List<Peer> getAllWellknownPeers();

  /**
   * Returns all pending transactions not yet in the blockchain
   * 
   * @return
   */
  public List<Transaction> getPendingTransactions();

  /**
   * Returns all pending aliases not yet in the blockchain
   * 
   * @return
   */
  public List<IAlias> getPendingAliases();

  /**
   * Creates a signed payment transaction
   * 
   * @param sender
   * @param recipient
   * @param amount
   * @param deadline
   * @param fee
   * @param referencedTransaction
   * @return
   * @throws TransactionException
   * @throws nxt.NxtException.ValidationException
   */
  public Transaction createPaymentTransaction(IAccount sender, Long recipient,
      long amountNQT, short deadline, long feeNQT,
      String referencedTransactionFullHash)
      throws TransactionException, ValidationException;

  /**
   * Creates an assign alias transaction
   * 
   * @param sender
   * @param alias
   * @param uri
   * @param deadline
   * @param fee
   * @param referencedTransaction
   * @return
   * @throws TransactionException
   * @throws ValidationException
   */
  public Transaction createAssignAliasTransaction(IAccount sender,
      String alias, String uri, short deadline, long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException;

  /**
   * Creates a send message transatcion
   * 
   * @param sender
   * @param _recipient
   * @param messageValue
   * @param deadline
   * @param fee
   * @param referencedTransaction
   * @return
   * @throws ValidationException
   * @throws TransactionException
   */
  public Transaction createSendMessageTransaction(IAccount sender,
      Long recipient, byte[] messageValue, short deadline, long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException;

  /**
   * Creates an issue asset transaction
   * 
   * @param sender
   * @param name
   * @param description
   * @param quantity
   * @param deadline
   * @param fee
   * @param referencedTransaction
   * @return
   */
  public Transaction createIssueAssetTransaction(IAccount sender, String name,
      String description, long quantityQNT, byte decimals, short deadline,
      long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException;

  /**
   * Creates an place ask order transaction
   * 
   * @param sender
   * @param asset
   * @param quantity
   * @param price
   * @param deadline
   * @param fee
   * @param referencedTransaction
   * @return
   * @throws TransactionException
   * @throws ValidationException
   */
  public Transaction createPlaceAskOrderTransaction(IAccount sender,
      long asset, long quantityQNT, long priceNQT, short deadline, long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException;

  /**
   * Creates an place bid order transaction
   * 
   * @param sender
   * @param asset
   * @param quantity
   * @param price
   *          in NXT cents
   * @param deadline
   * @param fee
   * @param referencedTransaction
   * @return
   * @throws TransactionException
   * @throws ValidationException
   */
  public Transaction createPlaceBidOrderTransaction(IAccount sender,
      long asset, long quantityQNT, long priceNQT, short deadline, long feeNQT,
      String referencedTransactionFullHash) throws ValidationException,
      TransactionException;

  /**
   * Creates a transfer asset transaction.
   * 
   * @param sender
   * @param recipient
   * @param asset
   * @param quantity
   * @param deadline
   * @param fee
   * @param referencedTransaction
   * @return
   * @throws TransactionException
   * @throws ValidationException
   */
  public Transaction createTransferAssetTransaction(IAccount sender,
      Long recipient, long asset, long quantityNQT, String comment,
      short deadline,
      long feeNQT, String referencedTransactionFullHash)
      throws ValidationException,
      TransactionException;

  /**
   * Creates a cancel ask order transactions
   * 
   * @param sender
   * @param order
   * @param deadline
   * @param fee
   * @param referencedTransaction
   * @return
   * @throws ValidationException
   * @throws TransactionException
   */
  public Transaction createCancelAskOrderTransaction(IAccount sender,
      Long order, short deadline, long feeNQT,
      String referencedTransactionFullHash)
      throws ValidationException, TransactionException;

  /**
   * Creates a cancel bid order transactions
   * 
   * @param sender
   * @param order
   * @param deadline
   * @param fee
   * @param referencedTransaction
   * @return
   * @throws ValidationException
   * @throws TransactionException
   */
  public Transaction createCancelBidOrderTransaction(IAccount sender,
      Long order, short deadline, long feeNQT,
      String referencedTransactionFullHash)
      throws ValidationException, TransactionException;

  /**
   * Returns the account number as String for a private key
   * 
   * @param privateKey
   * @return
   */
  public String getAccountForPrivateKey(String privateKey);

  /**
   * Returns the balance for an account, will return null for an invalid
   * account.
   * 
   * @param account
   * @return
   */
  public Long getBalanceForAccountNQT(String account);

  /**
   * Generates an authorizatoin token
   * 
   * @param privateKey
   * @param website
   * @return
   */
  public String generateAuthorizationToken(String privateKey, String website);

  /**
   * Get a token
   * 
   * @param website
   * @param token
   * @return
   */
  public Token getToken(String website, String token);

  /**
   * Returns the version number of NXT
   * 
   * @return
   */
  public String getSoftwareVersion();

  /**
   * Returns the total number of blocks (same as latestBlock.getHeight)
   * 
   * @return
   */
  public int getBlockCount();

  /**
   * Returns the Block with height
   * 
   * @param height
   * @return
   */
  public Block getBlockAtHeight(int height);

  /**
   * Convert timestamp
   * 
   * @param epochBasedTimestamp
   * @return
   */
  public long convertTimestamp(long epochBasedTimestamp);

  /**
   * Returns true or false about an alias being registered or not
   * 
   * @param alias
   * @return
   */
  public boolean aliasIsRegistered(String alias, IAccount account);

  /**
   * Returns the value of an alias as string
   * 
   * @param alias
   * @return
   */
  public String getAlias(String alias);

  /**
   * Returns a list of all trades for an asset
   * 
   * @param assetId
   * @return
   */
  public List<Trade> getTrades(Long assetId);

  /**
   * Returns a list of pending assets
   * 
   * @return
   */
  public List<IAsset> getPendingAssets();

}
