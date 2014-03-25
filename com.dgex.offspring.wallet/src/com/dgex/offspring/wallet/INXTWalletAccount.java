package com.dgex.offspring.wallet;

public interface INXTWalletAccount extends IWalletAccount {

  public String getPrivateKey();

  public String getAccountNumber();

  public long getBalance();

}
