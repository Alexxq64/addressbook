package com.addressbook.ui;

import com.addressbook.model.Contact;
import com.addressbook.model.Group;
import com.addressbook.model.Phone;
import com.addressbook.service.ContactService;
import com.addressbook.service.GroupService;
import com.addressbook.util.JsonExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends BorderPane {

    private final ContactService contactService = new ContactService();
    private final GroupService groupService = new GroupService();

    private final ObservableList<PhoneRow> allRows = FXCollections.observableArrayList();
    private final ObservableList<PhoneRow> visibleRows = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final TableView<PhoneRow> table = new TableView<>();

    private final Label detailName = new Label();
    private final VBox detailPhones = new VBox(5);
    private final Label detailEmail = new Label();
    private final Label detailGroup = new Label();
    private final Label detailAddress = new Label();
    private final Label detailNote = new Label();
    private final VBox detailPanel = new VBox(10);

    private boolean phonesMode = false;

    public MainWindow() {
        setPadding(new Insets(10));
        setTop(buildTopPanel());
        setCenter(buildCenter());
        setBottom(buildBottomPanel());
        buildTable();
        refresh();
    }

    private HBox buildTopPanel() {
        searchField.setPromptText("Поиск по ФИО...");
        searchField.setPrefWidth(300);
        searchField.setOnAction(e -> refresh());

        Button searchBtn = new Button("Найти");
        searchBtn.setOnAction(e -> refresh());

        Button clearBtn = new Button("Сбросить");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            refresh();
        });

        Button addBtn = new Button("Добавить");
        addBtn.setOnAction(e -> onAdd());

        ToggleGroup viewGroup = new ToggleGroup();
        RadioButton contactsView = new RadioButton("Контакты");
        RadioButton phonesView = new RadioButton("Телефоны");
        contactsView.setToggleGroup(viewGroup);
        phonesView.setToggleGroup(viewGroup);
        contactsView.setSelected(true);

        viewGroup.selectedToggleProperty().addListener((obs, old, sel) -> {
            phonesMode = (sel == phonesView);
            applyFilter();
        });

        HBox box = new HBox(10, new Label("Поиск:"), searchField, searchBtn, clearBtn,
                addBtn, new Separator(Orientation.VERTICAL), contactsView, phonesView);
        box.setPadding(new Insets(0, 0, 10, 0));
        return box;
    }

    private SplitPane buildCenter() {
        SplitPane split = new SplitPane();
        split.getItems().addAll(buildTablePane(), buildDetailPane());
        split.setDividerPositions(0.5);
        return split;
    }

    private VBox buildTablePane() {
        VBox box = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<PhoneRow, String> colLast = new TableColumn<>("Фамилия");
        colLast.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getContact().getLastName()));
        colLast.setPrefWidth(130);

        TableColumn<PhoneRow, String> colFirst = new TableColumn<>("Имя");
        colFirst.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getContact().getFirstName()));
        colFirst.setPrefWidth(110);

        TableColumn<PhoneRow, String> colPhone = new TableColumn<>("Телефон");
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPhone() != null ? c.getValue().getPhone().getNumber() : "—"));
        colPhone.setPrefWidth(150);

        TableColumn<PhoneRow, String> colType = new TableColumn<>("Тип");
        colType.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPhone() != null ? c.getValue().getPhone().getType() : "—"));
        colType.setPrefWidth(110);

        table.getColumns().addAll(colLast, colFirst, colPhone, colType);
        table.setItems(visibleRows);
        table.setPlaceholder(new Label("Ничего не найдено"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> showDetails(selected != null ? selected.getContact() : null));
    }

    private VBox buildDetailPane() {
        detailName.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label phonesTitle = new Label("Телефоны:");
        phonesTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label emailTitle = new Label("Email:");
        emailTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label groupTitle = new Label("Группа:");
        groupTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label addressTitle = new Label("Адрес:");
        addressTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label noteTitle = new Label("Заметка:");
        noteTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        detailPanel.getChildren().addAll(
                detailName, new Separator(),
                phonesTitle, detailPhones,
                emailTitle, detailEmail,
                groupTitle, detailGroup,
                addressTitle, detailAddress,
                noteTitle, detailNote
        );
        detailPanel.setPadding(new Insets(10));

        ScrollPane scroll = new ScrollPane(detailPanel);
        scroll.setFitToWidth(true);

        VBox box = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return box;
    }

    private HBox buildBottomPanel() {
        Button editBtn = new Button("Редактировать");
        editBtn.setOnAction(e -> onEdit());
        Button deleteBtn = new Button("Удалить");
        deleteBtn.setOnAction(e -> onDelete());
        Button groupsBtn = new Button("Группы");
        groupsBtn.setOnAction(e -> onGroups());
        Button exportBtn = new Button("Экспорт");
        exportBtn.setOnAction(e -> onExport());
        Button importBtn = new Button("Импорт");
        importBtn.setOnAction(e -> onImport());
        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> refresh());

        HBox box = new HBox(10, editBtn, deleteBtn, groupsBtn, exportBtn, importBtn, refreshBtn);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    private void refresh() {
        String query = searchField.getText();
        List<Contact> contacts = contactService.search(query);

        allRows.clear();
        for (Contact c : contacts) {
            if (c.getPhones().isEmpty()) {
                allRows.add(new PhoneRow(c, null));
            } else {
                for (Phone p : c.getPhones()) {
                    allRows.add(new PhoneRow(c, p));
                }
            }
        }
        applyFilter();
    }

    private void applyFilter() {
        if (phonesMode) {
            visibleRows.setAll(allRows);
        } else {
            List<PhoneRow> primary = new ArrayList<>();
            for (PhoneRow row : allRows) {
                if (row.isPrimary() || row.getPhone() == null) {
                    primary.add(row);
                }
            }
            visibleRows.setAll(primary);
        }
        showDetails(null);
    }

    private void showDetails(Contact c) {
        detailPhones.getChildren().clear();

        if (c == null) {
            detailName.setText("Выберите контакт");
            detailEmail.setText("");
            detailGroup.setText("");
            detailAddress.setText("");
            detailNote.setText("");
            return;
        }

        detailName.setText(c.getFullName());
        if (c.getPhones().isEmpty()) {
            detailPhones.getChildren().add(new Label("— нет телефонов —"));
        } else {
            for (Phone p : c.getPhones()) {
                detailPhones.getChildren().add(new Label("• " + p));
            }
        }
        detailEmail.setText(c.getEmail() != null ? c.getEmail() : "—");
        detailGroup.setText(resolveGroupName(c.getGroupId()));
        detailAddress.setText(c.getAddress() != null ? c.getAddress() : "—");
        detailNote.setText(c.getNote() != null ? c.getNote() : "—");
    }

    private String resolveGroupName(Long groupId) {
        if (groupId == null) return "—";
        return groupService.findAll().stream()
                .filter(g -> g.getId().equals(groupId))
                .map(Group::getName)
                .findFirst()
                .orElse("—");
    }

    private Contact selectedContact() {
        PhoneRow row = table.getSelectionModel().getSelectedItem();
        return row != null ? row.getContact() : null;
    }

    private void onAdd() {
        ContactForm form = new ContactForm(null);
        form.showAndWait().ifPresent(c -> refresh());
    }

    private void onEdit() {
        Contact selected = selectedContact();
        if (selected == null) {
            showError("Выберите контакт");
            return;
        }
        ContactForm form = new ContactForm(selected);
        form.showAndWait().ifPresent(c -> refresh());
    }

    private void onDelete() {
        Contact selected = selectedContact();
        if (selected == null) {
            showError("Выберите контакт");
            return;
        }
        contactService.delete(selected.getId());
        refresh();
    }

    private void onGroups() {
        GroupManager manager = new GroupManager();
        manager.showAndWait();
        refresh();
    }

    private void onExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Экспорт контактов");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        chooser.setInitialFileName("contacts.json");

        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) return;

        try {
            List<Contact> all = contactService.findAll();
            JsonExporter.export(all, file);
            showInfo("Экспортировано контактов: " + all.size());
        } catch (IOException e) {
            showError("Ошибка экспорта: " + e.getMessage());
        }
    }

    private void onImport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Импорт контактов");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));

        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file == null) return;

        try {
            List<Contact> imported = JsonExporter.importFrom(file);
            int added = 0;
            for (Contact c : imported) {
                c.setId(null);
                for (Phone p : c.getPhones()) {
                    p.setId(null);
                    p.setContactId(null);
                }
                try {
                    contactService.create(c);
                    added++;
                } catch (IllegalArgumentException ignored) {
                }
            }
            refresh();
            showInfo("Импортировано: " + added + " из " + imported.size());
        } catch (IOException e) {
            showError("Ошибка импорта: " + e.getMessage());
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}