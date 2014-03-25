package com.dgex.offspring.user.service;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;

public interface IUserService {

  public static final String TOPIC_USER_CREATED = "IUserService/TOPIC_USER_CREATED";
  public static final String TOPIC_USER_UPDATED = "IUserService/TOPIC_USER_UPDATED";

  public static final String TOPIC_BEFORE_USER_REMOVED = "IUserService/TOPIC_BEFORE_USER_REMOVED";
  public static final String TOPIC_USER_REMOVED = "IUserService/TOPIC_USER_REMOVED";

  public static final String TOPIC_BEFORE_ACTIVEUSER_CHANGED = "IUserService/TOPIC_BEFORE_ACTIVEUSER_CHANGED";
  public static final String TOPIC_ACTIVEUSER_CHANGED = "IUserService/TOPIC_ACTIVEUSER_CHANGED";

  public static final String USER_ID_KEY = "user.id";

  public void initialize(IEventBroker eventBroker, INxtService nxt);

  public IUser createUser(String name, String passphrase, String account);

  public void removeUser(IUser user);

  public List<IUser> getUsers();

  public IUser getActiveUser();

  public IUser findUserByAccount(IAccount account);

  public IUser findUser(Long accountId);

  public void removeAllUsers();

  public void setActiveUser(IUser user);

  public IUser getUser(IAccount account);
}