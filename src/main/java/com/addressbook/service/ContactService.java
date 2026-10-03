package com.addressbook.service;

import com.addressbook.dao.ContactDao;
import com.addressbook.model.Contact;

import java.util.List;

/**
 * Бизнес-логика работы с контактами.
 * Объединяет DAO и валидацию.
 */
public class ContactService {

    private final ContactDao contactDao = new ContactDao();
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
        if (contactDao.existsByPhone(c.getMobilePhone(), null)) {
            throw new IllegalArgumentException("Контакт с таким телефоном уже существует");
        }
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
        if (contactDao.existsByPhone(c.getMobilePhone(), c.getId())) {
            throw new IllegalArgumentException("Контакт с таким телефоном уже существует");
        }
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
}