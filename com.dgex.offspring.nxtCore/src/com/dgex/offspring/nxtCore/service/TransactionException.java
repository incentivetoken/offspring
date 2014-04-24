package com.dgex.offspring.nxtCore.service;

public class TransactionException extends Exception {

  public static final String INCORRECT_AMOUNT = "Incorrect amount";

  public static final String INCORRECT_FEE = "Incorrect fee";

  public static final String INCORRECT_DEADLINE = "Incorrect deadline";

  public static final String NOT_ENOUGH_FUNDS = "Not enough funds";

  public static final String INCORRECT_ALIAS_LENGTH = "Incorrect alias length";

  public static final String ALIAS_ALREADY_REGISTERED = "Alias already registered";

  public static final String INCORRECT_ALIAS = "Incorrect alias";

  public static final String INCORRECT_URI_LENGTH = "Incorrect uri length";

  public static final String INCORRECT_ARBITRARY_MESSAGE = "Incorrect arbitrary message";

  public static final String MISSING_NAME = "Missing name";

  public static final String INCORRECT_ASSET_NAME_LENGTH = "Incorrect asset name length";

  public static final String INCORRECT_ASSET_NAME = "Incorrect asset name";

  public static final String ASSET_NAME_ALREADY_USED = "Asset name already used";

  public static final String INCORRECT_ASSET_DESCRIPTION = "Incorrect asset descritption";

  public static final String INCORRECT_ASSET_QUANTITY = "Incorrect asset quantity";

  public static final String INCORRECT_PRICE = "Incorrect price";

  public static final String INCORRECT_QUANTITY = "Incorrect quantity";

  public static final String INTERNAL_ERROR = "Fatal internal error. Quit and restart application";

  public static final String UNKNOWN_ORDER = "Unknown order";

  public static final String UNKNOWN_ACCOUNT = "Unknown account";

  public static final String INCORRECT_DECIMALS = "Incorrect decimals";

  private final String type;

  public TransactionException(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

}
