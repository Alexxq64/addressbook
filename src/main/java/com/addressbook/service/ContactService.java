package com.addressbook.service;

import com.addressbook.dao.ContactDao;
import com.addressbook.dao.PhoneDao;
import com.addressbook.model.Contact;
import com.addressbook.model.Phone;

import java.util.List;

/**
 * Бизнес-логика работы с контактами.
 * Объединяет DAO и валидацию.
 */
public class ContactService {

    private final ContactDao contactDao = new ContactDao();
    private final PhoneDao phoneDao = new PhoneDao();
    private final ValidationService validation = new ValidationService();

    /**
     * Создаёт контакт с проверкой.
     * @throws IllegalArgumentException если данные не прошли валидацию.
     */
    public Contact create(Contact c) {
        List<String> errors = validation.validate(c);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
        checkPhoneDuplicates(c, null);
        return contactDao.save(c);
    }

    /**
     * Обновляет контакт с проверкой.
     */
    public void update(Contact c) {
        if (c.getId() == null) {
            throw new IllegalArgumentException("Контакт не сохранён");
        }
        List<String> errors = validation.validate(c);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
        checkPhoneDuplicates(c, c.getId());
        contactDao.update(c);
    }

    public void delete(Long id) {
        contactDao.delete(id);
    }

    public List<Contact> findAll() {
        return contactDao.findAll();
    }

    public List<Contact> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return contactDao.searchByName(query.trim());
    }

    public List<Contact> findByGroup(Long groupId) {
        return contactDao.findByGroup(groupId);
    }

    /**
     * Проверяет, что ни один телефон контакта не принадлежит другому контакту.
     */
    private void checkPhoneDuplicates(Contact c, Long excludeContactId) {
        for (Phone p : c.getPhones()) {
            if (p.getNumber() == null || p.getNumber().isBlank()) continue;
            if (phoneDao.existsByNumber(p.getNumber(), excludeContactId)) {
                throw new IllegalArgumentException(
                        "Телефон " + p.getNumber() + " уже используется другим контактом"
                );
            }
        }
    }
}