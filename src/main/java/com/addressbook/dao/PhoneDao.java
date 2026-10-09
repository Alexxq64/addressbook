package com.addressbook.dao;

import com.addressbook.model.Phone;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD-операции для телефонов контактов.
 */
public class PhoneDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    /**
     * Сохраняет список телефонов для контакта.
     * Старые телефоны контакта удаляются, новые вставляются.
     */
    public void saveAll(Long contactId, List<Phone> phones) {
        deleteByContactId(contactId);
        if (phones == null || phones.isEmpty()) return;

        String sql = "INSERT INTO phones (contact_id, number, type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            for (Phone p : phones) {
                ps.setLong(1, contactId);
                ps.setString(2, p.getNumber());
                ps.setString(3, p.getType());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения телефонов", e);
        }
    }

    /**
     * Загружает все телефоны контакта.
     */
    public List<Phone> findByContactId(Long contactId) {
        String sql = "SELECT id, contact_id, number, type FROM phones WHERE contact_id = ? ORDER BY id";
        List<Phone> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, contactId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки телефонов", e);
        }
        return result;
    }

    /**
     * Удаляет все телефоны контакта.
     */
    public void deleteByContactId(Long contactId) {
        String sql = "DELETE FROM phones WHERE contact_id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, contactId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления телефонов", e);
        }
    }

    /**
     * Проверяет, есть ли телефон с таким номером у другого контакта.
     */
    public boolean existsByNumber(String number, Long excludeContactId) {
        String sql = excludeContactId == null
                ? "SELECT COUNT(*) FROM phones WHERE number = ?"
                : "SELECT COUNT(*) FROM phones WHERE number = ? AND contact_id <> ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, number);
            if (excludeContactId != null) {
                ps.setLong(2, excludeContactId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки дубликата телефона", e);
        }
        return false;
    }

    private Phone mapRow(ResultSet rs) throws SQLException {
        Phone p = new Phone();
        p.setId(rs.getLong("id"));
        p.setContactId(rs.getLong("contact_id"));
        p.setNumber(rs.getString("number"));
        p.setType(rs.getString("type"));
        return p;
    }
}