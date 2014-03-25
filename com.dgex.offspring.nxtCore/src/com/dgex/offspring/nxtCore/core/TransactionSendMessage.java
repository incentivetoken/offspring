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

public class TransactionSendMessage {

  public static Transaction create(IAccount sender, IAccount _recipient,
      byte[] message, short deadline, int fee, Long referencedTransaction,
      INxtService nxt) throws ValidationException, TransactionException {

    String secretPhrase = sender.getPrivateKey();
    Long recipient = _recipient.getId();

    if (message.length == 0
        || message.length > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
      throw new TransactionException(
          TransactionException.INCORRECT_ARBITRARY_MESSAGE);
    }

    if ((fee <= 0) || (fee >= 1000000000L))
      throw new TransactionException(TransactionException.INCORRECT_FEE);

    if ((deadline < 1) || (deadline > 1440))
      throw new TransactionException(TransactionException.INCORRECT_DEADLINE);

    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);

    if ((account == null) || (fee * 100L > account.getUnconfirmedBalance()))
      throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);

    Attachment attachment = new Attachment.MessagingArbitraryMessage(message);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, recipient, 0, fee, referencedTransaction,
        attachment);

    transaction.sign(secretPhrase);
    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
