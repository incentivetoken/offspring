package com.dgex.offspring.config;

public class ContactsService implements IContactsService {

  static ContactsService INSTANCE = null;

  @Override
  public IContact getContact(final String account) {
    return new IContact() {

      @Override
      public String getName() {
        if (account.length() > 4) {
          return "Bella " + account.substring(0, 3);
        }
        return "Dennis";
      }
    };
  }

  public static IContactsService getInstance() {
    if (INSTANCE == null)
      INSTANCE = new ContactsService();
    return INSTANCE;
  }
}
