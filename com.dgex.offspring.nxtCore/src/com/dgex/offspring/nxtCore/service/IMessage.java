package com.dgex.offspring.nxtCore.service;

public interface IMessage {

  public IAccount getSender();

  public IAccount getRecipient();

  public String getMessage();

  public int getTimestamp();

  public Long getId();
}
