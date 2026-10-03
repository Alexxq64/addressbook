package com.addressbook.ui;

import com.addressbook.model.Contact;
import com.addressbook.service.ContactService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainWindow extends BorderPane {

    private final ContactService contactService = new ContactService();
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final TableView<Contact> table = new TableView<>();

    public MainWindow() {
        setPadding(new Insets(10));
        setTop(buildTopPanel());
        setCenter(buildTable());
        setBottom(buildBottomPanel());
        refresh();
    }

    private HBox buildTopPanel() {
        searchField.setPromptText("Поиск по ФИО...");
        searchField.setPrefWidth(300);

        Button searchBtn = new Button("Найти");
        searchBtn.setOnAction(e -> refresh());

        Button clearBtn = new Button("Сбросить");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            refresh();
        });

        Button addBtn = new Button("Добавить");
        addBtn.setOnAction(e -> onAdd());

        HBox box = new HBox(10, new Label("Поиск:"), searchField, searchBtn, clearBtn, addBtn);
        box.setPadding(new Insets(0, 0, 10, 0));
        return box;
    }

    @SuppressWarnings("unchecked")
    private TableView<Contact> buildTable() {
        TableColumn<Contact, String> colLast = new TableColumn<>("Фамилия");
        colLast.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLastName()));
        colLast.setPrefWidth(150);

        TableColumn<Contact, String> colFirst = new TableColumn<>("Имя");
        colFirst.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFirstName()));
        colFirst.setPrefWidth(120);

        TableColumn<Contact, String> colMiddle = new TableColumn<>("Отчество");
        colMiddle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMiddleName()));
        colMiddle.setPrefWidth(120);

        TableColumn<Contact, String> colPhone = new TableColumn<>("Мобильный");
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMobilePhone()));
        colPhone.setPrefWidth(150);

        TableColumn<Contact, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colEmail.setPrefWidth(180);

        table.getColumns().addAll(colLast, colFirst, colMiddle, colPhone, colEmail);
        table.setItems(contacts);
        table.setPlaceholder(new Label("Контактов пока нет"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return table;
    }

    private HBox buildBottomPanel() {
        Button editBtn = new Button("Редактировать");
        editBtn.setOnAction(e -> onEdit());

        Button deleteBtn = new Button("Удалить");
        deleteBtn.setOnAction(e -> onDelete());

        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> refresh());

        HBox box = new HBox(10, editBtn, deleteBtn, refreshBtn);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    private void refresh() {
        String query = searchField.getText();
        contacts.setAll(contactService.search(query));
    }

    private void onAdd() {
        System.out.println("Добавить контакт");
    }

    private void onEdit() {
        System.out.println("Редактировать контакт");
    }

    private void onDelete() {
        Contact selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            System.out.println("Ничего не выбрано");
            return;
        }
        contactService.delete(selected.getId());
        refresh();
    }
}