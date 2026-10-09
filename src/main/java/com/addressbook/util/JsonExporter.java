package com.addressbook.util;

import com.addressbook.model.Contact;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Экспорт и импорт контактов в JSON.
 */
public class JsonExporter {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static void export(List<Contact> contacts, File file) throws IOException {
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, contacts);
    }

    public static List<Contact> importFrom(File file) throws IOException {
        return MAPPER.readValue(file, new TypeReference<List<Contact>>() {});
    }
}