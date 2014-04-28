package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Attachment;
import nxt.Genesis;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;
import nxt.util.Convert;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionPlaceBidOrder extends TransactionBase {

  /* Price is in NXT cents */
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

    try {
      if (Convert.safeAdd(feeNQT, Convert.safeMultiply(priceNQT, quantityQNT)) > account
          .getUnconfirmedBalanceNQT()) {
        throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);
      }
    }
    catch (ArithmeticException e) {
      throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);
    }

    Attachment attachment = new Attachment.ColoredCoinsBidOrderPlacement(asset,
        quantityQNT, priceNQT);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, Genesis.CREATOR_ID, 0, feeNQT,
        referencedTransactionFullHash,
        attachment);
    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
