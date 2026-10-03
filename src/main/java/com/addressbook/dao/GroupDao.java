package com.addressbook.dao;

import com.addressbook.model.Group;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CRUD-операции для групп контактов.
 */
public class GroupDao {

    private final DatabaseManager db = DatabaseManager.getInstance();

    public Group save(Group group) {
        String sql = "INSERT INTO groups (name, description) VALUES (?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, group.getName());
            ps.setString(2, group.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    group.setId(keys.getLong(1));
                }
            }
            return group;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения группы", e);
        }
    }

    public void update(Group group) {
        String sql = "UPDATE groups SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, group.getName());
            ps.setString(2, group.getDescription());
            ps.setLong(3, group.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления группы", e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM groups WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления группы", e);
        }
    }

    public Optional<Group> findById(Long id) {
        String sql = "SELECT id, name, description FROM groups WHERE id = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска группы", e);
        }
        return Optional.empty();
    }

    public List<Group> findAll() {
        String sql = "SELECT id, name, description FROM groups ORDER BY name";
        List<Group> result = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения групп", e);
        }
        return result;
    }

    private Group mapRow(ResultSet rs) throws SQLException {
        Group g = new Group();
        g.setId(rs.getLong("id"));
        g.setName(rs.getString("name"));
        g.setDescription(rs.getString("description"));
        return g;
    }
}