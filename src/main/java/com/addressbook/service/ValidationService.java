package com.addressbook.service;

import com.addressbook.model.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Проверка корректности данных контакта перед сохранением.
 */
public class ValidationService {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Проверяет контакт и возвращает список ошибок.
     * Пустой список — данные корректны.
     */
    public List<String> validate(Contact c) {
        List<String> errors = new ArrayList<>();

        if (isBlank(c.getLastName())) {
            errors.add("Фамилия обязательна");
        } else if (c.getLastName().length() > 50) {
            errors.add("Фамилия не должна превышать 50 символов");
        }

        if (isBlank(c.getFirstName())) {
            errors.add("Имя обязательно");
        } else if (c.getFirstName().length() > 50) {
            errors.add("Имя не должно превышать 50 символов");
        }

        if (isBlank(c.getMobilePhone())) {
            errors.add("Мобильный телефон обязателен");
        } else if (!PHONE_PATTERN.matcher(c.getMobilePhone()).matches()) {
            errors.add("Неверный формат мобильного телефона (пример: +79001234567)");
        }

        if (!isBlank(c.getWorkPhone()) && !PHONE_PATTERN.matcher(c.getWorkPhone()).matches()) {
            errors.add("Неверный формат рабочего телефона");
        }

        if (!isBlank(c.getEmail()) && !EMAIL_PATTERN.matcher(c.getEmail()).matches()) {
            errors.add("Неверный формат email");
        }

        if (!isBlank(c.getAddress()) && c.getAddress().length() > 200) {
            errors.add("Адрес не должен превышать 200 символов");
        }

        if (!isBlank(c.getNote()) && c.getNote().length() > 500) {
            errors.add("Заметка не должна превышать 500 символов");
        }

        return errors;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}