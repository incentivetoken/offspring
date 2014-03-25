package com.dgex.offspring.user.internal;

import org.apache.log4j.Logger;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.user.service.IUser;

public class UserImpl implements IUser {

  static Logger logger = Logger.getLogger(UserImpl.class);

  private IAccount account = null;

  private String name = null;

  public UserImpl(String name, IAccount account) {
    this.name = name;
    this.account = account;
  }

  @Override
  public String toString() {
    return super.toString() + " " + account;
  }

  @Override
  public IAccount getAccount() {
    return account;
  }

  @Override
  public String getName() {
    return name;
  }
}