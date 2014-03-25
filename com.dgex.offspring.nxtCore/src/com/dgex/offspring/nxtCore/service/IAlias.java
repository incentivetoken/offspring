package com.dgex.offspring.nxtCore.service;

import nxt.Account;

public interface IAlias {

  public String getName();

  public String getURI();

  public Account getAccount();

  public long getTimestamp();

  public Long getId();
}
