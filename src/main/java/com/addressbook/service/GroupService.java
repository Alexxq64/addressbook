package com.addressbook.service;

import com.addressbook.dao.GroupDao;
import com.addressbook.model.Group;

import java.util.List;

/**
 * Бизнес-логика работы с группами.
 */
public class GroupService {

    private final GroupDao groupDao = new GroupDao();

    public Group create(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название группы обязательно");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Название группы не должно превышать 100 символов");
        }
        return groupDao.save(new Group(name.trim(), description));
    }

    public void update(Group group) {
        if (group.getId() == null) {
            throw new IllegalArgumentException("Группа не сохранена");
        }
        if (group.getName() == null || group.getName().isBlank()) {
            throw new IllegalArgumentException("Название группы обязательно");
        }
        groupDao.update(group);
    }

    public void delete(Long id) {
        groupDao.delete(id);
    }

    public List<Group> findAll() {
        return groupDao.findAll();
    }
}