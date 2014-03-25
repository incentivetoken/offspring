package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Alias;
import nxt.Attachment;
import nxt.Genesis;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionAssignAlias {

  public static Transaction create(IAccount sender, String alias, String uri,
      short deadline, int fee, Long referencedTransaction, INxtService nxt)
      throws TransactionException, ValidationException {

    String secretPhrase = sender.getPrivateKey();
    alias = alias.trim();
    if ((alias.length() == 0) || (alias.length() > 100))
      throw new TransactionException(
          TransactionException.INCORRECT_ALIAS_LENGTH);

    String normalizedAlias = alias.toLowerCase();
    for (int i = 0; i < normalizedAlias.length(); i++) {
      if ("0123456789abcdefghijklmnopqrstuvwxyz".indexOf(normalizedAlias
          .charAt(i)) < 0) {
        throw new TransactionException(TransactionException.INCORRECT_ALIAS);
      }
    }

    uri = uri.trim();
    if (uri.length() > 1000) {
      throw new TransactionException(TransactionException.INCORRECT_URI_LENGTH);
    }

    if ((fee <= 0) || (fee >= 1000000000L))
      throw new TransactionException(TransactionException.INCORRECT_FEE);

    if ((deadline < 1) || (deadline > 1440))
      throw new TransactionException(TransactionException.INCORRECT_DEADLINE);

    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);
    if ((account == null) || (fee * 100L > account.getUnconfirmedBalance())) {
      throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);
    }

    Alias aliasData = Alias.getAlias(normalizedAlias);
    if ((aliasData != null) && (aliasData.getAccount() != account))
      throw new TransactionException(
          TransactionException.ALIAS_ALREADY_REGISTERED);

    Attachment attachment = new Attachment.MessagingAliasAssignment(alias, uri);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, Genesis.CREATOR_ID, 0, fee, referencedTransaction,
        attachment);

    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
