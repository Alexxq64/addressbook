package com.addressbook.dao;

import com.addressbook.model.Contact;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CRUD-операции для контактов.
 */
public class ContactDao {

    private final DatabaseManager db = DatabaseManager.getInstance();
    private final PhoneDao phoneDao = new PhoneDao();

    public Contact save(Contact c) {
        String sql = """
                INSERT INTO contacts (last_name, first_name, middle_name,
                    email, address, group_id, note, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        LocalDateTime now = LocalDateTime.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);

        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getLastName());
            ps.setString(2, c.getFirstName());
            ps.setString(3, c.getMiddleName());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getAddress());
            if (c.getGroupId() == null) {
                ps.setNull(6, Types.BIGINT);
            } else {
                ps.setLong(6, c.getGroupId());
            }
            ps.setString(7, c.getNote());
            ps.setTimestamp(8, Timestamp.valueOf(now));
            ps.setTimestamp(9, Timestamp.valueOf(now));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getLong(1));
                }
            }
            phoneDao.saveAll(c.getId(), c.getPhones());
            return c;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения контакта", e);
        }
    }

    public void update(Contact c) {
        String sql = """
                UPDATE contacts SET last_name = ?, first_name = ?, middle_name = ?,
                    email = ?, address = ?, group_id = ?, note = ?, updated_at = ?
                WHERE id = ?
                """;
        c.setUpdatedAt(LocalDateTime.now());
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, c.getLastName());
            ps.setString(2, c.getFirstName());
            ps.setString(3, c.getMiddleName());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getAddress());
            if (c.getGroupId() == null) {
                ps.setNull(6, Types.BIGINT);
            } else {
                ps.setLong(6, c.getGroupId());
            }
            ps.setString(7, c.getNote());
            ps.setTimestamp(8, Timestamp.valueOf(c.getUpdatedAt()));
            ps.setLong(9, c.getId());
            ps.executeUpdate();

            phoneDao.saveAll(c.getId(), c.getPhones());
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления контакта", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM contacts WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления контакта", e);
        }
    }

    public Optional<Contact> findById(Long id) {
        String sql = "SELECT * FROM contacts WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contact c = mapRow(rs);
                    c.setPhones(phoneDao.findByContactId(c.getId()));
                    return Optional.of(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска контакта", e);
        }
        return Optional.empty();
    }

    public List<Contact> findAll() {
        String sql = "SELECT * FROM contacts ORDER BY last_name, first_name";
        return queryList(sql);
    }

    public List<Contact> searchByName(String query) {
        String sql = """
                SELECT DISTINCT c.* FROM contacts c
                LEFT JOIN phones p ON p.contact_id = c.id
                WHERE LOWER(c.last_name) LIKE ?
                   OR LOWER(c.first_name) LIKE ?
                   OR LOWER(c.middle_name) LIKE ?
                   OR LOWER(p.number) LIKE ?
                ORDER BY c.last_name, c.first_name
                """;
        String pattern = "%" + query.toLowerCase() + "%";
        List<Contact> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Contact c = mapRow(rs);
                    c.setPhones(phoneDao.findByContactId(c.getId()));
                    result.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска", e);
        }
        return result;
    }

    public List<Contact> findByGroup(Long groupId) {
        String sql = "SELECT * FROM contacts WHERE group_id = ? ORDER BY last_name, first_name";
        List<Contact> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Contact c = mapRow(rs);
                    c.setPhones(phoneDao.findByContactId(c.getId()));
                    result.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации по группе", e);
        }
        return result;
    }

    private List<Contact> queryList(String sql) {
        List<Contact> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Contact c = mapRow(rs);
                c.setPhones(phoneDao.findByContactId(c.getId()));
                result.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения контактов", e);
        }
        return result;
    }

    private Contact mapRow(ResultSet rs) throws SQLException {
        Contact c = new Contact();
        c.setId(rs.getLong("id"));
        c.setLastName(rs.getString("last_name"));
        c.setFirstName(rs.getString("first_name"));
        c.setMiddleName(rs.getString("middle_name"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        long groupId = rs.getLong("group_id");
        c.setGroupId(rs.wasNull() ? null : groupId);
        c.setNote(rs.getString("note"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) c.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) c.setUpdatedAt(updated.toLocalDateTime());
        return c;
    }
}