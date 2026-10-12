package com.addressbook.service;

import com.addressbook.model.Contact;
import com.addressbook.model.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationServiceTest {

    private ValidationService validation;

    @BeforeEach
    void setUp() {
        validation = new ValidationService();
    }

    @Test
    void validContact_noErrors() {
        Contact c = new Contact();
        c.setLastName("Иванов");
        c.setFirstName("Иван");
        c.getPhones().add(new Phone("+79001234567", "мобильный"));

        List<String> errors = validation.validate(c);

        assertTrue(errors.isEmpty(), "Ожидалось отсутствие ошибок, получено: " + errors);
    }

    @Test
    void emptyLastName_error() {
        Contact c = new Contact();
        c.setFirstName("Иван");
        c.getPhones().add(new Phone("+79001234567", "мобильный"));

        List<String> errors = validation.validate(c);

        assertTrue(errors.contains("Фамилия обязательна"));
    }

    @Test
    void emptyFirstName_error() {
        Contact c = new Contact();
        c.setLastName("Иванов");
        c.getPhones().add(new Phone("+79001234567", "мобильный"));

        List<String> errors = validation.validate(c);

        assertTrue(errors.contains("Имя обязательно"));
    }

    @Test
    void noPhones_error() {
        Contact c = new Contact();
        c.setLastName("Иванов");
        c.setFirstName("Иван");

        List<String> errors = validation.validate(c);

        assertTrue(errors.contains("Добавьте хотя бы один телефон"));
    }

    @Test
    void invalidPhone_error() {
        Contact c = new Contact();
        c.setLastName("Иванов");
        c.setFirstName("Иван");
        c.getPhones().add(new Phone("123", "мобильный"));

        List<String> errors = validation.validate(c);

        assertTrue(errors.stream().anyMatch(e -> e.contains("неверный формат")));
    }

    @Test
    void invalidEmail_error() {
        Contact c = new Contact();
        c.setLastName("Иванов");
        c.setFirstName("Иван");
        c.setEmail("not-an-email");
        c.getPhones().add(new Phone("+79001234567", "мобильный"));

        List<String> errors = validation.validate(c);

        assertTrue(errors.contains("Неверный формат email"));
    }

    @Test
    void validEmail_noError() {
        Contact c = new Contact();
        c.setLastName("Иванов");
        c.setFirstName("Иван");
        c.setEmail("ivan@example.com");
        c.getPhones().add(new Phone("+79001234567", "мобильный"));

        List<String> errors = validation.validate(c);

        assertFalse(errors.contains("Неверный формат email"));
    }

    @Test
    void tooLongLastName_error() {
        Contact c = new Contact();
        c.setLastName("А".repeat(51));
        c.setFirstName("Иван");
        c.getPhones().add(new Phone("+79001234567", "мобильный"));

        List<String> errors = validation.validate(c);

        assertTrue(errors.stream().anyMatch(e -> e.contains("не должна превышать 50")));
    }
}