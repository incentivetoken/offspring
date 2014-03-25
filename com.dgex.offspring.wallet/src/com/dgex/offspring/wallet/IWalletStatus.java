package com.dgex.offspring.wallet;

import java.io.File;

public interface IWalletStatus {

  public static final int SUCCESS = 1;
  public static final int FAILURE = 2;

  /**
   * The IWallet that was backed up.
   */
  public IWallet getWallet();

  /**
   * The File the backup was written to
   */
  public File getBackupFile();

  /**
   * SUCCESS or FAILURE
   */
  public int getStatus();

  /**
   * Throwable caught when saving (overwriting) the users wallet file
   */
  public Throwable getThrowable();

}
