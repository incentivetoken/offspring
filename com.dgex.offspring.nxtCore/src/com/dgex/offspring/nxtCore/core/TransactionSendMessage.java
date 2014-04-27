package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Attachment;
import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionSendMessage extends TransactionBase {

  public static Transaction create(IAccount sender, Long recipient,
      byte[] message, short deadline, long feeNQT,
      String referencedTransactionFullHash,
      INxtService nxt) throws ValidationException, TransactionException {

    String secretPhrase = sender.getPrivateKey();
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);
    validate(account, 0, feeNQT, deadline);

    if (message.length == 0
        || message.length > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
      throw new TransactionException(
          TransactionException.INCORRECT_ARBITRARY_MESSAGE);
    }

    Attachment attachment = new Attachment.MessagingArbitraryMessage(message);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, recipient, 0, feeNQT,
        referencedTransactionFullHash,
        attachment);

    transaction.sign(secretPhrase);
    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
