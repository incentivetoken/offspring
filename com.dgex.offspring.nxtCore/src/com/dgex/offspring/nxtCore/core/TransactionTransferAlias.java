package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Alias;
import nxt.Attachment;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionTransferAlias extends TransactionBase {

  public static Transaction create(IAccount sender, Long recipient, long aliasId, short deadline, long feeNQT,
      String referencedTransactionFullHash,
      NXTService nxt) throws TransactionException, ValidationException {

    String secretPhrase = sender.getPrivateKey();
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);
    validate(account, 0, feeNQT, deadline);
    validateAlias(Long.valueOf(aliasId));

    Alias alias = Alias.getAlias(aliasId);
    if (!alias.getAccount().equals(account)) {
      throw new TransactionException(TransactionException.INCORRECT_ALIAS);
    }

    Attachment attachment = null; //new Attachment.ColoredCoinsAliasTransfer(aliasId);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, recipient, 0, feeNQT,
        referencedTransactionFullHash,
        attachment);
    transaction.sign(secretPhrase);

    System.out.println("Alias transfer disabled...");
    // nxt.broacastTransaction(transaction);

    return transaction;
  }
}
