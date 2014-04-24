package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Attachment;
import nxt.Genesis;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Order;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionCancelAskOrder extends TransactionBase {

  public static Transaction create(IAccount sender, Long order, short deadline,
      long feeNQT, String referencedTransactionFullHash, NXTService nxt)
      throws TransactionException, ValidationException {

    String secretPhrase = sender.getPrivateKey();
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);
    validate(account, 0, feeNQT, deadline);

    Order.Ask orderData = Order.Ask.getAskOrder(order);
    if (orderData == null
        || !orderData.getAccount().getId().equals(account.getId()))
      throw new TransactionException(TransactionException.UNKNOWN_ORDER);

    Attachment attachment = new Attachment.ColoredCoinsAskOrderCancellation(
        order);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, Genesis.CREATOR_ID, 0, feeNQT,
        referencedTransactionFullHash, attachment);
    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
