package com.addressbook.ui;

import com.addressbook.model.Contact;
import com.addressbook.model.Phone;

/**
 * Строка таблицы: контакт + один из его телефонов.
 * Используется для режима «Телефоны».
 */
public class PhoneRow {

    private final Contact contact;
    private final Phone phone;

    public PhoneRow(Contact contact, Phone phone) {
        this.contact = contact;
        this.phone = phone;
    }

    public Contact getContact() {
        return contact;
    }

    public Phone getPhone() {
        return phone;
    }

    public boolean isPrimary() {
        return phone != null && !contact.getPhones().isEmpty()
                && phone == contact.getPhones().get(0);
    }
}