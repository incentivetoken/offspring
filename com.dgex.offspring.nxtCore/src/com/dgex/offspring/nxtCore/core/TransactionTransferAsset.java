package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Attachment;
import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.crypto.Crypto;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionTransferAsset {

  public static Transaction create(IAccount sender, Long recipient, long asset,
      int quantity, short deadline, int fee, Long referencedTransaction,
      NXTService nxt) throws TransactionException, ValidationException {

    String secretPhrase = sender.getPrivateKey();

    if ((quantity <= 0) || (quantity >= Constants.MAX_ASSET_QUANTITY))
      throw new TransactionException(TransactionException.INCORRECT_AMOUNT);

    if ((fee <= 0) || (fee >= 1000000000L))
      throw new TransactionException(TransactionException.INCORRECT_FEE);

    if ((deadline < 1) || (deadline > 1440))
      throw new TransactionException(TransactionException.INCORRECT_DEADLINE);

    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);

    Integer assetBalance = account.getUnconfirmedAssetBalance(asset);
    if (assetBalance == null || quantity > assetBalance) {
      throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);
    }

    Attachment attachment = new Attachment.ColoredCoinsAssetTransfer(asset,
        quantity, null);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, recipient, 0, fee, referencedTransaction,
        attachment);
    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
