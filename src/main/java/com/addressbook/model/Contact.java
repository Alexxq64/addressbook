package com.addressbook.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Контакт в адресной книге.
 * Телефоны хранятся отдельно — у одного контакта их может быть несколько.
 */
public class Contact {

    private Long id;
    private String lastName;
    private String firstName;
    private String middleName;
    private String email;
    private String address;
    private Long groupId;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Phone> phones = new ArrayList<>();

    public Contact() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones != null ? phones : new ArrayList<>();
    }

    /**
     * Полное ФИО одной строкой.
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (lastName != null) sb.append(lastName);
        if (firstName != null) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(firstName);
        }
        if (middleName != null && !middleName.isBlank()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(middleName);
        }
        return sb.toString();
    }

    /**
     * Первый телефон из списка — для отображения в таблице.
     */
    public String getPrimaryPhone() {
        return phones.isEmpty() ? "" : phones.get(0).getNumber();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(id, contact.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getFullName();
    }
}