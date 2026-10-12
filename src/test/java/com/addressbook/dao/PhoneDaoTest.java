package com.addressbook.dao;

import com.addressbook.model.Contact;
import com.addressbook.model.Phone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhoneDaoTest {

    private PhoneDao phoneDao;
    private ContactDao contactDao;
    private final List<Long> createdContactIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        DatabaseManager.getInstance().initSchema();
        phoneDao = new PhoneDao();
        contactDao = new ContactDao();
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdContactIds) {
            try {
                contactDao.delete(id);
            } catch (Exception ignored) {
            }
        }
        createdContactIds.clear();
    }

    @Test
    void saveAll_storesPhones() {
        Contact c = createContact(uniquePhone());
        List<Phone> loaded = phoneDao.findByContactId(c.getId());

        assertEquals(1, loaded.size());
        assertEquals(c.getPhones().get(0).getNumber(), loaded.get(0).getNumber());
        assertEquals("мобильный", loaded.get(0).getType());
    }

    @Test
    void saveAll_multiplePhones() {
        Contact c = new Contact();
        c.setLastName("ТестовыйМноготел");
        c.setFirstName("Тест");
        c.getPhones().add(new Phone(uniquePhone(), "мобильный"));
        c.getPhones().add(new Phone(uniquePhone(), "рабочий"));
        c.getPhones().add(new Phone(uniquePhone(), "домашний"));
        contactDao.save(c);
        createdContactIds.add(c.getId());

        List<Phone> loaded = phoneDao.findByContactId(c.getId());

        assertEquals(3, loaded.size());
    }

    @Test
    void saveAll_replacesOldPhones() {
        Contact c = createContact(uniquePhone());
        Long contactId = c.getId();

        c.getPhones().clear();
        c.getPhones().add(new Phone(uniquePhone(), "рабочий"));
        phoneDao.saveAll(contactId, c.getPhones());

        List<Phone> loaded = phoneDao.findByContactId(contactId);

        assertEquals(1, loaded.size());
        assertEquals("рабочий", loaded.get(0).getType());
    }

    @Test
    void deleteByContactId_removesAll() {
        Contact c = createContact(uniquePhone());
        Long contactId = c.getId();

        phoneDao.deleteByContactId(contactId);

        assertTrue(phoneDao.findByContactId(contactId).isEmpty());
    }

    @Test
    void existsByNumber_returnsTrue() {
        String phone = uniquePhone();
        createContact(phone);

        assertTrue(phoneDao.existsByNumber(phone, null));
    }

    @Test
    void existsByNumber_returnsFalse() {
        assertFalse(phoneDao.existsByNumber("+79999999999", null));
    }

    @Test
    void existsByNumber_excludesOwnContact() {
        String phone = uniquePhone();
        Contact c = createContact(phone);

        assertFalse(phoneDao.existsByNumber(phone, c.getId()),
                "Свой же номер не должен считаться дубликатом");
    }

    private Contact createContact(String phone) {
        Contact c = new Contact();
        c.setLastName("Тестовый");
        c.setFirstName("Контакт");
        c.getPhones().add(new Phone(phone, "мобильный"));
        Contact saved = contactDao.save(c);
        createdContactIds.add(saved.getId());
        return saved;
    }

    private String uniquePhone() {
        return "+7910" + String.format("%07d", System.nanoTime() % 10000000L);
    }
}