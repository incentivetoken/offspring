package com.dgex.offspring.nxtCore.core;

import nxt.Attachment;
import nxt.Constants;
import nxt.Genesis;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionPlaceBidOrder {

  /* Price is in NXT cents */
  public static Transaction create(IAccount sender, long asset, int quantity,
      long price, short deadline, int fee, Long referencedTransaction,
      NXTService nxt) throws TransactionException, ValidationException {

    String secretPhrase = sender.getPrivateKey();
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);

    if ((quantity <= 0) || (quantity >= Constants.MAX_ASSET_QUANTITY))
      throw new TransactionException(TransactionException.INCORRECT_AMOUNT);

    if ((fee <= 0) || (fee >= 1000000000L))
      throw new TransactionException(TransactionException.INCORRECT_FEE);

    if ((deadline < 1) || (deadline > 1440))
      throw new TransactionException(TransactionException.INCORRECT_DEADLINE);

    if (price <= 0 || price > Constants.MAX_BALANCE * 100L)
      throw new TransactionException(TransactionException.INCORRECT_PRICE);

    if (quantity <= 0 || quantity > Constants.MAX_ASSET_QUANTITY)
      throw new TransactionException(TransactionException.INCORRECT_QUANTITY);

    Attachment attachment = new Attachment.ColoredCoinsBidOrderPlacement(asset,
        quantity, price);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, Genesis.CREATOR_ID, 0, fee, referencedTransaction,
        attachment);
    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
