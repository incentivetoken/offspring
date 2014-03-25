package com.dgex.offspring.wallet;

import java.io.File;
import java.util.List;

@SuppressWarnings("serial")
public interface IWallet {

  public static class WalletNotInitializedException extends Exception {}

  public static class WalletInitializedException extends Exception {}

  public static class WalletInvalidPassword extends Exception {

    public WalletInvalidPassword(Throwable trowable) {
      super(trowable);
    }

    public WalletInvalidPassword() {
      super();
    }
  }

  public static class DuplicateAccountException extends Exception {}

  public static class AccountNotFoundException extends Exception {}

  public static class WalletVerifyException extends Exception {

    public WalletVerifyException(Throwable trowable) {
      super(trowable);
    }

    public WalletVerifyException() {
      super();
    }
  }

  public static class WalletBackupException extends Exception {

    public WalletBackupException(Throwable trowable) {
      super(trowable);
    }

    public WalletBackupException() {
      super();
    }
  }

  public static class WalletSaveException extends Exception {

    public WalletSaveException(Throwable trowable) {
      super(trowable);
    }

    public WalletSaveException() {
      super();
    }
  }

  /**
   * If the backup file exists it is decrypted and verified, if that fails a
   * WalletInvalidPassword exception is thrown.
   * 
   * @throws WalletInvalidPasswordException
   */
  public void initialize(String password) throws WalletInvalidPassword;

  /**
   * The default wallet file
   * 
   * @return
   */
  public File getDefaultWalletFile();

  /**
   * Returns the list of IAccounts in the order they where added to the wallet.
   * 
   * @returns ordered list of IWalletAccount
   * 
   * @throws WalletNotInitializedException
   */
  public List<IWalletAccount> getAccounts()
      throws WalletNotInitializedException;

  /**
   * Atomic add of account. Algorithm:
   * 
   * 1. backup wallet 2. verify backup 3. overwrite wallet 4. verify wallet
   * 
   * In case of unsuccessfull save IWalletStatus.status will contain
   * IWalletStatus.FAILURE use IWalletStatus.getBackupFile to help the user in
   * restoring the original wallet.
   * 
   * @param walletAccount
   * @return IWalletStatus
   * @throws WalletNotInitializedException
   * @throws DuplicateAccountException
   * @throws WalletBackupException
   */
  public IWalletStatus addAccount(IWalletAccount walletAccount)
      throws WalletNotInitializedException, DuplicateAccountException,
      WalletBackupException;

  /**
   * Atomic removal of account. Algorithm:
   * 
   * 1. backup wallet 2. verify backup 3. overwrite wallet 4. verify wallet
   * 
   * In case of unsuccessfull save IWalletStatus.status will contain
   * IWalletStatus.FAILURE use IWalletStatus.getBackupFile to help the user in
   * restoring the original wallet.
   * 
   * @param walletAccount
   * @return IWalletStatus
   * @throws WalletNotInitializedException
   * @throws AccountNotFoundException
   * @throws WalletBackupException
   */
  public IWalletStatus removeAccount(IWalletAccount walletAccount)
      throws WalletNotInitializedException, AccountNotFoundException,
      WalletBackupException;

  /**
   * The File that contains the wallet data
   */
  public File getWalletFile();

  /**
   * Set the wallet file you want to use. This can only be done before you
   * initialize the wallet. In case the wallet was already initialized you
   * should call clear first.
   * 
   * @throws WalletInitializedException
   */
  public void setWalletFile(File file) throws WalletInitializedException;

  /**
   * If the wallet file was not saved to disk before, this will write the
   * (empty) wallet to disk. If the user exits the account creation process
   * early he will loose his wallet since it is not saved.
   */
  public void createWalletFile() throws WalletSaveException;

  /**
   * Set wallet to uninitialized state
   */
  public void clear();
}
