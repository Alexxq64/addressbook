package com.addressbook.dao;

import com.addressbook.model.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GroupDaoTest {

    private GroupDao dao;

    @BeforeEach
    void setUp() {
        DatabaseManager.getInstance().initSchema();
        dao = new GroupDao();
    }

    @Test
    void save_assignsId() {
        Group g = new Group("Тестовая группа", "Описание");
        Group saved = dao.save(g);

        assertNotNull(saved.getId(), "ID должен быть присвоен");
    }

    @Test
    void findById_returnsGroup() {
        Group saved = dao.save(new Group("Семья", "Родственники"));

        Optional<Group> found = dao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Семья", found.get().getName());
        assertEquals("Родственники", found.get().getDescription());
    }

    @Test
    void findAll_returnsAllGroups() {
        int before = dao.findAll().size();

        dao.save(new Group("Группа 1", null));
        dao.save(new Group("Группа 2", null));

        List<Group> all = dao.findAll();

        assertEquals(before + 2, all.size());
    }

    @Test
    void update_changesName() {
        Group saved = dao.save(new Group("Старое имя", null));
        saved.setName("Новое имя");

        dao.update(saved);

        Optional<Group> found = dao.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Новое имя", found.get().getName());
    }

    @Test
    void delete_removesGroup() {
        Group saved = dao.save(new Group("Удаляемая", null));
        Long id = saved.getId();

        dao.delete(id);

        Optional<Group> found = dao.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void findById_notFound_returnsEmpty() {
        Optional<Group> found = dao.findById(-999L);
        assertFalse(found.isPresent());
    }
}