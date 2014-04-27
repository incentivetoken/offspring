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

public class TransactionIssueAsset extends TransactionBase {

  public static Transaction create(IAccount sender, String name,
      String description, long quantityQNT, byte decimals, short deadline,
      long feeNQT, String referencedTransactionFullHash, INxtService nxt)
      throws TransactionException,
      ValidationException {

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

    if (decimals < 0 || decimals > 8) {
      throw new TransactionException(TransactionException.INCORRECT_DECIMALS);
    }

    String secretPhrase = sender.getPrivateKey();
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);
    validate(account, 0, feeNQT, Constants.ASSET_ISSUANCE_FEE_NQT, deadline);
    validateQuantityQNT(quantityQNT);

    Attachment attachment = new Attachment.ColoredCoinsAssetIssuance(name,
        description, quantityQNT, decimals);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, Genesis.CREATOR_ID, 0, feeNQT,
        referencedTransactionFullHash,
        attachment);

    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
