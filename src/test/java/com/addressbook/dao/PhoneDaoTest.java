package com.addressbook.dao;

import com.addressbook.model.Contact;
import com.addressbook.model.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhoneDaoTest {

    private PhoneDao phoneDao;
    private ContactDao contactDao;

    @BeforeEach
    void setUp() {
        DatabaseManager.getInstance().initSchema();
        phoneDao = new PhoneDao();
        contactDao = new ContactDao();
    }

    @Test
    void saveAll_storesPhones() {
        Contact c = createContact("+79001111111");
        Long contactId = c.getId();

        List<Phone> loaded = phoneDao.findByContactId(contactId);

        assertEquals(1, loaded.size());
        assertEquals("+79001111111", loaded.get(0).getNumber());
        assertEquals("мобильный", loaded.get(0).getType());
    }

    @Test
    void saveAll_multiplePhones() {
        Contact c = new Contact();
        c.setLastName("Многотелефонный");
        c.setFirstName("Тест");
        c.getPhones().add(new Phone("+79002222222", "мобильный"));
        c.getPhones().add(new Phone("+74952222222", "рабочий"));
        c.getPhones().add(new Phone("+74992222222", "домашний"));
        contactDao.save(c);

        List<Phone> loaded = phoneDao.findByContactId(c.getId());

        assertEquals(3, loaded.size());
    }

    @Test
    void saveAll_replacesOldPhones() {
        Contact c = createContact("+79003333333");
        Long contactId = c.getId();

        c.getPhones().clear();
        c.getPhones().add(new Phone("+79004444444", "рабочий"));
        phoneDao.saveAll(contactId, c.getPhones());

        List<Phone> loaded = phoneDao.findByContactId(contactId);

        assertEquals(1, loaded.size());
        assertEquals("+79004444444", loaded.get(0).getNumber());
    }

    @Test
    void deleteByContactId_removesAll() {
        Contact c = createContact("+79005555555");
        Long contactId = c.getId();

        phoneDao.deleteByContactId(contactId);

        List<Phone> loaded = phoneDao.findByContactId(contactId);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void existsByNumber_returnsTrue() {
        createContact("+79006666666");

        boolean exists = phoneDao.existsByNumber("+79006666666", null);

        assertTrue(exists);
    }

    @Test
    void existsByNumber_returnsFalse() {
        boolean exists = phoneDao.existsByNumber("+79999999999", null);
        assertFalse(exists);
    }

    @Test
    void existsByNumber_excludesOwnContact() {
        String uniquePhone = "+79" + (System.nanoTime() % 1000000000L);
        Contact c = createContact(uniquePhone);
        Long contactId = c.getId();

        boolean exists = phoneDao.existsByNumber(uniquePhone, contactId);

        assertFalse(exists, "Свой же номер не должен считаться дубликатом");
    }

    private Contact createContact(String phone) {
        Contact c = new Contact();
        c.setLastName("Тестовый");
        c.setFirstName("Контакт");
        c.getPhones().add(new Phone(phone, "мобильный"));
        return contactDao.save(c);
    }
}