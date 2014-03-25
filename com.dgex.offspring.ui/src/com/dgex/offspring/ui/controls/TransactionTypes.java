package com.dgex.offspring.ui.controls;

import nxt.Transaction;

public class TransactionTypes {

  private static final byte TYPE_PAYMENT = 0;
  private static final byte TYPE_MESSAGING = 1;
  private static final byte TYPE_COLORED_COINS = 2;
  private static final byte SUBTYPE_PAYMENT_ORDINARY_PAYMENT = 0;
  private static final byte SUBTYPE_MESSAGING_ARBITRARY_MESSAGE = 0;
  private static final byte SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT = 1;
  private static final byte SUBTYPE_MESSAGING_POLL_CREATION = 2;
  private static final byte SUBTYPE_MESSAGING_VOTE_CASTING = 3;
  private static final byte SUBTYPE_COLORED_COINS_ASSET_ISSUANCE = 0;
  private static final byte SUBTYPE_COLORED_COINS_ASSET_TRANSFER = 1;
  private static final byte SUBTYPE_COLORED_COINS_ASK_ORDER_PLACEMENT = 2;
  private static final byte SUBTYPE_COLORED_COINS_BID_ORDER_PLACEMENT = 3;
  private static final byte SUBTYPE_COLORED_COINS_ASK_ORDER_CANCELLATION = 4;
  private static final byte SUBTYPE_COLORED_COINS_BID_ORDER_CANCELLATION = 5;

  public static String getTransactionType(Transaction t) {
    byte type = t.getType().getType();
    byte subtype = t.getType().getSubtype();
    switch (type) {
    case TYPE_PAYMENT:
      switch (subtype) {
      case SUBTYPE_PAYMENT_ORDINARY_PAYMENT:
        return "payment";
      default:
        return "";
      }
    case TYPE_MESSAGING:
      switch (subtype) {
      case SUBTYPE_MESSAGING_ARBITRARY_MESSAGE:
        return "message";
      case SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT:
        return "alias";
      case SUBTYPE_MESSAGING_POLL_CREATION:
        return "poll";
      case SUBTYPE_MESSAGING_VOTE_CASTING:
        return "vote";
      default:
        return "";
      }
    case TYPE_COLORED_COINS:
      switch (subtype) {
      case SUBTYPE_COLORED_COINS_ASSET_ISSUANCE:
        return "issue asset";
      case SUBTYPE_COLORED_COINS_ASSET_TRANSFER:
        return "transfer asset";
      case SUBTYPE_COLORED_COINS_ASK_ORDER_PLACEMENT:
        return "ask order";
      case SUBTYPE_COLORED_COINS_BID_ORDER_PLACEMENT:
        return "bid order";
      case SUBTYPE_COLORED_COINS_ASK_ORDER_CANCELLATION:
        return "ask order cancel";
      case SUBTYPE_COLORED_COINS_BID_ORDER_CANCELLATION:
        return "bid order cancel";
      default:
        return "";
      }
    default:
      return "";
    }
  }
}
