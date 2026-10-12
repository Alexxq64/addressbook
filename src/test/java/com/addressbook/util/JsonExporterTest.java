package com.addressbook.util;

import com.addressbook.model.Contact;
import com.addressbook.model.Phone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void exportAndImport_oneContact_dataPreserved() throws Exception {
        Contact c = new Contact();
        c.setId(1L);
        c.setLastName("Иванов");
        c.setFirstName("Иван");
        c.setEmail("ivan@example.com");
        c.getPhones().add(new Phone("+79001234567", "мобильный"));
        c.getPhones().add(new Phone("+74951234567", "рабочий"));

        File file = tempDir.resolve("contacts.json").toFile();

        JsonExporter.export(List.of(c), file);

        assertTrue(file.exists(), "Файл должен быть создан");
        assertTrue(file.length() > 0, "Файл не должен быть пустым");

        List<Contact> loaded = JsonExporter.importFrom(file);

        assertEquals(1, loaded.size());
        Contact loadedContact = loaded.get(0);
        assertEquals("Иванов", loadedContact.getLastName());
        assertEquals("Иван", loadedContact.getFirstName());
        assertEquals("ivan@example.com", loadedContact.getEmail());
        assertEquals(2, loadedContact.getPhones().size());
        assertEquals("+79001234567", loadedContact.getPhones().get(0).getNumber());
        assertEquals("мобильный", loadedContact.getPhones().get(0).getType());
    }

    @Test
    void exportAndImport_multipleContacts() throws Exception {
        Contact c1 = new Contact();
        c1.setLastName("Иванов");
        c1.setFirstName("Иван");
        c1.getPhones().add(new Phone("+79001234567", "мобильный"));

        Contact c2 = new Contact();
        c2.setLastName("Петров");
        c2.setFirstName("Пётр");
        c2.getPhones().add(new Phone("+79007654321", "рабочий"));

        File file = tempDir.resolve("contacts.json").toFile();
        JsonExporter.export(List.of(c1, c2), file);

        List<Contact> loaded = JsonExporter.importFrom(file);

        assertEquals(2, loaded.size());
        assertEquals("Иванов", loaded.get(0).getLastName());
        assertEquals("Петров", loaded.get(1).getLastName());
    }

    @Test
    void export_emptyList() throws Exception {
        File file = tempDir.resolve("empty.json").toFile();
        JsonExporter.export(List.of(), file);

        assertTrue(file.exists());
        List<Contact> loaded = JsonExporter.importFrom(file);
        assertTrue(loaded.isEmpty());
    }

    @Test
    void importFrom_nonExistentFile_throws() {
        File file = tempDir.resolve("missing.json").toFile();
        assertThrows(Exception.class, () -> JsonExporter.importFrom(file));
    }
}