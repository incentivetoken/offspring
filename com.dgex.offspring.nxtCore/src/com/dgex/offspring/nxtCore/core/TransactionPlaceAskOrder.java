package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Attachment;
import nxt.Genesis;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionPlaceAskOrder extends TransactionBase {

  public static Transaction create(IAccount sender, long asset,
      long quantityQNT, long priceNQT, short deadline, long feeNQT,
      String referencedTransactionFullHash,
      NXTService nxt) throws TransactionException, ValidationException {

    String secretPhrase = sender.getPrivateKey();
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);
    validate(account, 0, feeNQT, deadline);
    validatePriceNQT(priceNQT);
    validateQuantityQNT(quantityQNT);
    validateAsset(asset);

    Long assetBalance = account.getUnconfirmedAssetBalanceQNT(asset);
    if (assetBalance == null || quantityQNT > assetBalance) {
      throw new TransactionException(TransactionException.NOT_ENOUGH_ASSETS);
    }

    Attachment attachment = new Attachment.ColoredCoinsAskOrderPlacement(asset,
        quantityQNT, priceNQT);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, Genesis.CREATOR_ID, 0, feeNQT,
        referencedTransactionFullHash, attachment);
    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
