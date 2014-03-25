package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Attachment;
import nxt.Constants;
import nxt.Genesis;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionIssueAsset {

  public static Transaction create(IAccount sender, String name,
      String description, int quantity, short deadline, int fee,
      Long referencedTransaction, INxtService nxt) throws TransactionException,
      ValidationException {

    String secretPhrase = sender.getPrivateKey();
    if (name == null) {
      throw new TransactionException(TransactionException.MISSING_NAME);
    }

    name = name.trim();
    if (name.length() < 3 || name.length() > 10) {
      throw new TransactionException(
          TransactionException.INCORRECT_ASSET_NAME_LENGTH);
    }

    String normalizedName = name.toLowerCase();
    for (int i = 0; i < normalizedName.length(); i++) {
      if (Constants.ALPHABET.indexOf(normalizedName.charAt(i)) < 0) {
        throw new TransactionException(
            TransactionException.INCORRECT_ASSET_NAME);
      }
    }

    if (description != null && description.length() > 1000) {
      throw new TransactionException(
          TransactionException.INCORRECT_ASSET_DESCRIPTION);
    }

    if (quantity <= 0 || quantity > Constants.MAX_ASSET_QUANTITY) {
      throw new TransactionException(
          TransactionException.INCORRECT_ASSET_QUANTITY);
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

    Attachment attachment = new Attachment.ColoredCoinsAssetIssuance(name,
        description, quantity);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, Genesis.CREATOR_ID, 0, fee, referencedTransaction,
        attachment);

    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
