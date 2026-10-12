package com.addressbook.dao;

import com.addressbook.model.Group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GroupDaoTest {

    private GroupDao dao;
    private final List<Long> createdIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        DatabaseManager.getInstance().initSchema();
        dao = new GroupDao();
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdIds) {
            try {
                dao.delete(id);
            } catch (Exception ignored) {
            }
        }
        createdIds.clear();
    }

    @Test
    void save_assignsId() {
        Group saved = createGroup("Тестовая группа", "Описание");
        assertNotNull(saved.getId(), "ID должен быть присвоен");
    }

    @Test
    void findById_returnsGroup() {
        Group saved = createGroup("ТестоваяСемья", "Родственники");

        Optional<Group> found = dao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("ТестоваяСемья", found.get().getName());
        assertEquals("Родственники", found.get().getDescription());
    }

    @Test
    void findAll_returnsAllGroups() {
        int before = dao.findAll().size();

        createGroup("ТестоваяГруппа1", null);
        createGroup("ТестоваяГруппа2", null);

        assertEquals(before + 2, dao.findAll().size());
    }

    @Test
    void update_changesName() {
        Group saved = createGroup("ТестовоеСтароеИмя", null);
        saved.setName("ТестовоеНовоеИмя");

        dao.update(saved);

        Optional<Group> found = dao.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("ТестовоеНовоеИмя", found.get().getName());
    }

    @Test
    void delete_removesGroup() {
        Group saved = createGroup("ТестоваяУдаляемая", null);
        Long id = saved.getId();

        dao.delete(id);
        createdIds.remove(id);

        assertFalse(dao.findById(id).isPresent());
    }

    @Test
    void findById_notFound_returnsEmpty() {
        Optional<Group> found = dao.findById(-999L);
        assertFalse(found.isPresent());
    }

    private Group createGroup(String name, String description) {
        Group g = new Group(name, description);
        Group saved = dao.save(g);
        createdIds.add(saved.getId());
        return saved;
    }
}