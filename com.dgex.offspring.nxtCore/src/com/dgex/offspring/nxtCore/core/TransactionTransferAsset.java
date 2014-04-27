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

public class TransactionTransferAsset extends TransactionBase {

  public static Transaction create(IAccount sender, Long recipient,
 long asset,
      long quantityQNT, String comment, short deadline, long feeNQT,
      String referencedTransactionFullHash,
      NXTService nxt) throws TransactionException, ValidationException {

    String secretPhrase = sender.getPrivateKey();
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    Account account = Account.getAccount(publicKey);
    validate(account, 0, feeNQT, deadline);
    validateAsset(asset);
    validateQuantityQNT(quantityQNT);

    if (comment.length() > Constants.MAX_ASSET_TRANSFER_COMMENT_LENGTH) {
      throw new TransactionException(
          TransactionException.INCORRECT_ASSET_TRANSFER_COMMENT);
    }

    Long assetBalance = account.getUnconfirmedAssetBalanceQNT(asset);
    if (assetBalance == null || quantityQNT > assetBalance) {
      throw new TransactionException(TransactionException.NOT_ENOUGH_ASSETS);
    }

    Attachment attachment = new Attachment.ColoredCoinsAssetTransfer(asset,
        quantityQNT, comment);

    Transaction transaction = Nxt.getTransactionProcessor().newTransaction(
        deadline, publicKey, recipient, 0, feeNQT,
        referencedTransactionFullHash,
        attachment);
    transaction.sign(secretPhrase);

    nxt.broacastTransaction(transaction);

    return transaction;
  }
}
