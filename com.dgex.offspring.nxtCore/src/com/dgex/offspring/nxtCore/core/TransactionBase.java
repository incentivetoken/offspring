package com.dgex.offspring.nxtCore.core;

import nxt.Account;
import nxt.Alias;
import nxt.Asset;
import nxt.Constants;
import nxt.util.Convert;

import com.dgex.offspring.nxtCore.service.TransactionException;

public class TransactionBase {

  static void validate(Account senderAccount, long amountNQT,
      long feeNQT, long minimumFeeNQT, short deadline)
      throws TransactionException {

    if (senderAccount == null)
      throw new TransactionException(TransactionException.UNKNOWN_ACCOUNT);

    if (feeNQT < minimumFeeNQT || feeNQT >= Constants.MAX_BALANCE_NQT)
      throw new TransactionException(TransactionException.INCORRECT_FEE);

    if ((deadline < 1) || (deadline > 1440))
      throw new TransactionException(TransactionException.INCORRECT_DEADLINE);

    try {
      if (Convert.safeAdd(amountNQT, feeNQT) > senderAccount
          .getUnconfirmedBalanceNQT()) {
        throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);
      }
    }
    catch (ArithmeticException e) {
      throw new TransactionException(TransactionException.NOT_ENOUGH_FUNDS);
    }
  }

  static void validate(Account senderAccount, long amountNQT,
      long feeNQT, short deadline) throws TransactionException {
    validate(senderAccount, amountNQT, feeNQT, Constants.ONE_NXT, deadline);
  }

  static void validateAmountNQT(long amountNQT) throws TransactionException {
    if (amountNQT <= 0 || amountNQT >= Constants.MAX_BALANCE_NQT) {
      throw new TransactionException(
          TransactionException.INCORRECT_AMOUNT);
    }
  }

  static void validateFeeNQT(long feeNQT) throws TransactionException {
    if (feeNQT <= 0 || feeNQT >= Constants.MAX_BALANCE_NQT) {
      throw new TransactionException(
          TransactionException.INCORRECT_FEE);
    }
  }

  static void validatePriceNQT(long priceNQT) throws TransactionException {
    if (priceNQT <= 0 || priceNQT > Constants.MAX_BALANCE_NQT) {
      throw new TransactionException(
          TransactionException.INCORRECT_PRICE);
    }
  }

  static void validateAsset(Long assetId) throws TransactionException {
    Asset asset;
    try {
        asset = Asset.getAsset(assetId);
    } catch (RuntimeException e) {
      throw new TransactionException(
          TransactionException.INCORRECT_ASSET);
    }
    if (asset == null) {
      throw new TransactionException(
          TransactionException.UNKNOWN_ASSET);
    }
  }
  
  static void validateAlias(Long aliasId) throws TransactionException {
    Alias alias;
    try {
      alias = Alias.getAlias(aliasId);
    } catch (RuntimeException e) {
      throw new TransactionException(TransactionException.INCORRECT_ALIAS);
    }
    if (alias == null) {
      throw new TransactionException(TransactionException.UNKNOWN_ALIAS);
    }
  }

  static void validateQuantityQNT(long quantityQNT) throws TransactionException {
    if (quantityQNT <= 0 || quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT) {
      throw new TransactionException(
          TransactionException.INCORRECT_QUANTITY);
    }
  }
  
}
