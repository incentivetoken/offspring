package com.dgex.offspring.wallet;

import java.io.File;

public class WalletStatus implements IWalletStatus {

  public File backupFile = null;
  public int status = IWalletStatus.SUCCESS;
  public Throwable throwable = null;
  private IWallet wallet = null;

  public WalletStatus(IWallet wallet) {
    this.wallet = wallet;
  }

  @Override
  public IWallet getWallet() {
    return wallet;
  }

  @Override
  public File getBackupFile() {
    return backupFile;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public Throwable getThrowable() {
    return throwable;
  }

}