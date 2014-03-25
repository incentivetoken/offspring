package com.dgex.offspring.nxtCore.core;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.IMessage;

public class MessageHelper implements IMessage {

  private final IAccount sender;
  private final IAccount recipient;
  private final String message;
  private final int timestamp;
  private final long id;

  public MessageHelper(IAccount sender, IAccount recipient, String message,
      int timestamp, long id) {
    this.sender = sender;
    this.recipient = recipient;
    this.message = message;
    this.timestamp = timestamp;
    this.id = id;
  }

  @Override
  public IAccount getSender() {
    return sender;
  }

  @Override
  public IAccount getRecipient() {
    return recipient;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public int getTimestamp() {
    return timestamp;
  }

  @Override
  public Long getId() {
    return id;
  }
}
