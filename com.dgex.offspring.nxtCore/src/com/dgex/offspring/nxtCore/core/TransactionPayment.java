package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionPayment extends TransactionBase {

  public static Transaction create(IAccount sender, Long recipient,
      long amountNQT, short deadline, long feeNQT,
      String referencedTransactionFullHash, INxtService nxt)
      throws ValidationException, TransactionException {

    String secretPhrase = sender.getPrivateKey();
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);
    validate(account, amountNQT, feeNQT, deadline);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, recipient, amountNQT, feeNQT,
        referencedTransactionFullHash);
    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
