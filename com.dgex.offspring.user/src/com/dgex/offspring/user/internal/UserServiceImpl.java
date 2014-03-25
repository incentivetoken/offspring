package com.dgex.offspring.user.internal;

import java.util.ArrayList;
import java.util.List;

import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.services.events.IEventBroker;

import com.dgex.offspring.nxtCore.core.AccountHelper;
import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class UserServiceImpl implements IUserService {

  static Logger logger = Logger.getLogger(UserServiceImpl.class);

  private IEventBroker broker = null;

  private INxtService nxt = null;

  private final ArrayList<IUser> users;

  private IUser activeUser = null;

  public UserServiceImpl() {
    users = new ArrayList<IUser>();
  }

  @Override
  public void initialize(IEventBroker broker, INxtService nxt) {
    this.nxt = nxt;
    this.broker = broker;
  }

  @Override
  public List<IUser> getUsers() {
    return users;
  }

  @Override
  public IUser createUser(String name, String passphrase, String accountNo) {
    IUser user = null;
    for (IUser u : users) {
      if (accountNo.equals(u.getAccount().getStringId())) {
        user = u;
        break;
      }
    }
    if (user == null) {
      IAccount account;
      if (passphrase != null) {
        account = nxt.unlock(passphrase);
      }
      else {
        account = new AccountHelper(nxt, Convert.parseUnsignedLong(accountNo
            .trim()));
      }
      user = new UserImpl(name, account);
      users.add(user);
      broker.send(IUserService.TOPIC_USER_CREATED, user);
      account.startForging();
    }
    return user;
  }

  @Override
  public void setActiveUser(IUser user) {
    logger.trace("setActiveUser old=" + activeUser + " new=" + user);
    broker.send(IUserService.TOPIC_BEFORE_ACTIVEUSER_CHANGED, user);
    activeUser = user;
    broker.send(IUserService.TOPIC_ACTIVEUSER_CHANGED, user);
  }

  @Override
  public IUser getActiveUser() {
    return activeUser;
  }

  @Override
  public IUser findUserByAccount(IAccount account) {
    return findUser(account.getId());
  }

  @Override
  public IUser findUser(Long accountId) {
    for (IUser user : users) {
      if (user.getAccount().getId().equals(accountId)) {
        return user;
      }
    }
    return null;
  }

  @Override
  public void removeUser(IUser user) {
    if (users.contains(user)) {
      broker.send(IUserService.TOPIC_BEFORE_USER_REMOVED, user);
      users.remove(user);
      broker.send(IUserService.TOPIC_USER_REMOVED, user);
      if (user.equals(activeUser))
        setActiveUser(null);
    }
  }

  @Override
  public void removeAllUsers() {
    IUser[] list = users.toArray(new IUser[users.size()]);
    for (int i = 0; i < list.length; i++) {
      removeUser(list[i]);
    }
  }

  @Override
  public IUser getUser(IAccount account) {
    for (IUser user : users) {
      if (user.getAccount().equals(account))
        return user;
    }
    return null;
  }
}
