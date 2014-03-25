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

public class TransactionCancelBidOrder {

  public static Transaction create(IAccount sender, Long order, short deadline,
      int fee, Long referencedTransaction, NXTService nxt)
      throws TransactionException, ValidationException {

    String secretPhrase = sender.getPrivateKey();
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);

    if ((fee <= 0) || (fee >= 1000000000L))
      throw new TransactionException(TransactionException.INCORRECT_FEE);

    if ((deadline < 1) || (deadline > 1440))
      throw new TransactionException(TransactionException.INCORRECT_DEADLINE);

    Account account = Account.getAccount(publicKey);
    if (account == null)
      throw new TransactionException(TransactionException.INTERNAL_ERROR);

    Order.Bid orderData = Order.Bid.getBidOrder(order);
    if (orderData == null
        || !orderData.getAccount().getId().equals(account.getId()))
      throw new TransactionException(TransactionException.UNKNOWN_ORDER);

    Attachment attachment = new Attachment.ColoredCoinsBidOrderCancellation(
        order);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, Genesis.CREATOR_ID, 0, fee, referencedTransaction,
        attachment);
    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
