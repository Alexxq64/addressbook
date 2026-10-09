package com.addressbook.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Управляет подключением к встроенной базе данных H2.
 * База хранится в файле data/addressbook.mv.db рядом с проектом.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./data/addressbook";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Возвращает открытое соединение с БД.
     * Если соединения нет — создаёт новое.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    /**
     * Создаёт таблицы, если они ещё не существуют.
     * Вызывается один раз при старте приложения.
     */
    public void initSchema() {
        String createGroups = """
                CREATE TABLE IF NOT EXISTS groups (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    description VARCHAR(255)
                )
                """;

        String createContacts = """
                CREATE TABLE IF NOT EXISTS contacts (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    last_name VARCHAR(50) NOT NULL,
                    first_name VARCHAR(50) NOT NULL,
                    middle_name VARCHAR(50),
                    email VARCHAR(100),
                    address VARCHAR(200),
                    group_id BIGINT,
                    note VARCHAR(500),
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP,
                    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE SET NULL
                )
                """;

        String createPhones = """
                CREATE TABLE IF NOT EXISTS phones (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    contact_id BIGINT NOT NULL,
                    number VARCHAR(20) NOT NULL,
                    type VARCHAR(20),
                    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE
                )
                """;

        try (Statement st = getConnection().createStatement()) {
            st.execute(createGroups);
            st.execute(createContacts);
            st.execute(createPhones);
            System.out.println("Схема БД инициализирована");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания схемы БД", e);
        }
    }

    /**
     * Закрывает соединение с БД.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}