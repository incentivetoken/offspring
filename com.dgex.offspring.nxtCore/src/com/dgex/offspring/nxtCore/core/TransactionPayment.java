package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionPayment {

  public static Transaction create(IAccount sender, Long recipient, int amount,
      short deadline, int fee, Long referencedTransaction, INxtService nxt)
      throws ValidationException, TransactionException {

    String secretPhrase = sender.getPrivateKey();

    if ((amount <= 0) || (amount >= 1000000000L))
      throw new TransactionException(TransactionException.INCORRECT_AMOUNT);

    if ((fee <= 0) || (fee >= 1000000000L))
      throw new TransactionException(TransactionException.INCORRECT_FEE);

    if ((deadline < 1) || (deadline > 1440))
      throw new TransactionException(TransactionException.INCORRECT_DEADLINE);

    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);
    if ((account == null)
        || ((amount + fee) * 100L > account.getUnconfirmedBalance()))
      throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, recipient, amount, fee, referencedTransaction);
    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
