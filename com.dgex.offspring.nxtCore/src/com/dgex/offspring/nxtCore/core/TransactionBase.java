package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Constants;
import nxt.util.Convert;

import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionBase {

  public static void validate(Account senderAccount, long amountNQT,
      long feeNQT, long minimumFeeNQT, short deadline)
      throws TransactionException {
    if (senderAccount == null)
      throw new TransactionException(TransactionException.UNKNOWN_ACCOUNT);

    if (feeNQT < minimumFeeNQT)
      throw new TransactionException(TransactionException.INCORRECT_FEE);

    if ((deadline < 1) || (deadline > 1440))
      throw new TransactionException(TransactionException.INCORRECT_DEADLINE);

    try {
      if (Convert.safeAdd(amountNQT, feeNQT) > senderAccount
          .getUnconfirmedBalanceNQT()) {
        throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);
      }
    }
    catch (ArithmeticException e) {
      throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);
    }
  }

  public static void validate(Account senderAccount, long amountNQT,
      long feeNQT, short deadline) throws TransactionException {
    validate(senderAccount, amountNQT, feeNQT, Constants.ONE_NXT, deadline);
  }
}
