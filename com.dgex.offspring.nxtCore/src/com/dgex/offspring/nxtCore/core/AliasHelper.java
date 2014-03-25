package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Alias;
import nxt.util.Convert;

import com.dgex.offspring.nxtCore.service.IAlias;

public class AliasHelper implements IAlias {

  private final String name;
  private final String uri;
  private final Account account;
  private final int timestamp;
  private final Long id;

  public AliasHelper(String name, String uri, Account account) {
    this.name = name;
    this.uri = uri;
    this.account = account;
    this.timestamp = Convert.getEpochTime();
    this.id = 0l;
  }

  public AliasHelper(Alias alias) {
    this.name = alias.getAliasName();
    this.uri = alias.getURI();
    this.account = alias.getAccount();
    this.timestamp = alias.getTimestamp();
    this.id = alias.getId();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof IAlias && name.equals(((IAlias) other).getName());
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getURI() {
    return uri;
  }

  @Override
  public Account getAccount() {
    return account;
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public Long getId() {
    return id;
  }

}
