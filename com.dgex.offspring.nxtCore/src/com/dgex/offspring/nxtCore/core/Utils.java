package com.dgex.offspring.nxtCore.core;

import nxt.Account;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;

public class Utils {

  public static boolean filterTransaction(INxtService nxt, Long sender,
      Long receiver) {
    for (IAccount account : nxt.getUnlockedAccounts()) {
      Account _acc = account.getNative();
      if (_acc != null) {
        Long other = _acc.getId();
        if (other.equals(sender) || other.equals(receiver)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean filterAccount(INxtService nxt, Long id) {
    for (IAccount account : nxt.getUnlockedAccounts()) {
      Account _acc = account.getNative();
      if (_acc != null) {
        Long other = _acc.getId();
        if (other.equals(id)) {
          return true;
        }
      }
    }
    return false;
  }

}
