package com.addressbook.service;

import com.addressbook.dao.ContactDao;
import com.addressbook.dao.DatabaseManager;
import com.addressbook.model.Contact;
import com.addressbook.model.Phone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContactServiceTest {

    private ContactService contactService;
    private ContactDao contactDao;
    private final List<Long> createdIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        DatabaseManager.getInstance().initSchema();
        contactService = new ContactService();
        contactDao = new ContactDao();
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdIds) {
            try { contactDao.delete(id); } catch (Exception ignored) {}
        }
        createdIds.clear();
    }

    @Test
    void create_validContact_success() {
        Contact c = buildContact("ТестовоеСервис1", uniquePhone());
        Contact saved = contactService.create(c);
        createdIds.add(saved.getId());
        assertNotNull(saved.getId());
    }

    @Test
    void create_invalidPhone_throws() {
        Contact c = buildContact("ТестовоеСервис2", "123");
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> contactService.create(c)
        );
        assertTrue(ex.getMessage().contains("неверный формат"));
    }

    @Test
    void create_emptyLastName_throws() {
        Contact c = new Contact();
        c.setFirstName("ТестовоеИмя");
        c.getPhones().add(new Phone(uniquePhone(), "мобильный"));
        assertThrows(IllegalArgumentException.class, () -> contactService.create(c));
    }

    @Test
    void create_duplicatePhone_throws() {
        String phone = uniquePhone();

        Contact c1 = buildContact("ТестовоеДубликат1", phone);
        Contact saved = contactService.create(c1);
        createdIds.add(saved.getId());

        Contact c2 = buildContact("ТестовоеДубликат2", phone);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> contactService.create(c2)
        );
        assertTrue(ex.getMessage().contains("уже используется"));
    }

    @Test
    void update_changesData() {
        Contact saved = contactService.create(buildContact("ТестовоеДоИзм", uniquePhone()));
        createdIds.add(saved.getId());
        saved.setLastName("ТестовоеПослеИзм");

        contactService.update(saved);

        List<Contact> all = contactService.findAll();
        assertTrue(all.stream().anyMatch(c ->
                c.getId().equals(saved.getId()) && "ТестовоеПослеИзм".equals(c.getLastName())));
    }

    @Test
    void delete_removesContact() {
        Contact saved = contactService.create(buildContact("ТестовоеУдаляемое", uniquePhone()));
        Long id = saved.getId();

        contactService.delete(id);
        createdIds.remove(id);

        List<Contact> all = contactService.findAll();
        assertFalse(all.stream().anyMatch(c -> c.getId().equals(id)));
    }

    @Test
    void search_findsByLastName() {
        String uniqueLast = "УникальныйСервис" + (System.nanoTime() % 100000);
        Contact saved = contactService.create(buildContact(uniqueLast, uniquePhone()));
        createdIds.add(saved.getId());

        List<Contact> found = contactService.search(uniqueLast);

        assertFalse(found.isEmpty());
    }

    @Test
    void search_emptyQuery_returnsAll() {
        List<Contact> all = contactService.search("");
        assertEquals(contactService.findAll().size(), all.size());
    }

    @Test
    void search_nullQuery_returnsAll() {
        List<Contact> all = contactService.search(null);
        assertEquals(contactService.findAll().size(), all.size());
    }

    private Contact buildContact(String lastName, String phone) {
        Contact c = new Contact();
        c.setLastName(lastName);
        c.setFirstName("ТестовоеИмя");
        c.getPhones().add(new Phone(phone, "мобильный"));
        return c;
    }

    private String uniquePhone() {
        return "+7910" + String.format("%07d", System.nanoTime() % 10000000L);
    }
}