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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalTime;

public class MainWindow extends BorderPane {

    private final ContactService contactService = new ContactService();
    private final GroupService groupService = new GroupService();

    private final ObservableList<PhoneRow> allRows = FXCollections.observableArrayList();
    private final ObservableList<PhoneRow> visibleRows = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final TableView<PhoneRow> table = new TableView<>();

    private final Label detailName = new Label();
    private final VBox detailPhones = new VBox(5);
    private final Button detailCopyBtn = new Button("📋");
    private final Button detailEditBtn = new Button("✎");
    private final Button detailDeleteBtn = new Button("🗑");
    private final Label detailEmail = new Label();
    private final ComboBox<Group> detailGroupBox = new ComboBox<>();
    private final Button groupEditBtn = new Button("⚙");
    private final Button groupRemoveBtn = new Button("🗑");
    private final Label detailAddress = new Label();
    private final Label detailNote = new Label();
    private final VBox detailPanel = new VBox(10);

    private boolean phonesMode = false;
    private boolean suppressGroupAction = false;

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
                (obs, old, selected) -> {
                    log("table selection changed: " + (selected == null ? "null" : selected.getContact().getFullName()));
                    showDetails(selected != null ? selected.getContact() : null);
                });
    }

    private VBox buildDetailPane() {
        detailName.setFont(Font.font("System", FontWeight.BOLD, 16));

        detailCopyBtn.setTooltip(new Tooltip("Копировать карточку"));
        detailCopyBtn.setStyle("-fx-font-size: 14; -fx-padding: 2 8 2 8;");
        detailCopyBtn.setOnAction(e -> onCopyCard());

        detailEditBtn.setTooltip(new Tooltip("Редактировать контакт"));
        detailEditBtn.setStyle("-fx-font-size: 14; -fx-padding: 2 8 2 8;");
        detailEditBtn.setOnAction(e -> onEdit());

        detailDeleteBtn.setTooltip(new Tooltip("Удалить контакт"));
        detailDeleteBtn.setStyle("-fx-font-size: 14; -fx-padding: 2 8 2 8;");
        detailDeleteBtn.setOnAction(e -> onDelete());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox nameRow = new HBox(8, detailName, spacer, detailCopyBtn, detailEditBtn, detailDeleteBtn);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label phonesTitle = new Label("Телефоны");
        phonesTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label emailTitle = new Label("Email");
        emailTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        Label groupTitle = new Label("Группа");
        groupTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        groupEditBtn.setTooltip(new Tooltip("Редактор групп"));
        groupEditBtn.setStyle("-fx-font-size: 14; -fx-padding: 2 8 2 8;");
        groupEditBtn.setOnAction(e -> onGroups());

        groupRemoveBtn.setTooltip(new Tooltip("Убрать группу у контакта"));
        groupRemoveBtn.setStyle("-fx-font-size: 12; -fx-padding: 1 6 1 6;");
        groupRemoveBtn.setOnAction(e -> onRemoveGroupFromContact());

        detailGroupBox.setPrefWidth(180);
        detailGroupBox.setPromptText("— без группы —");
        detailGroupBox.setOnAction(e -> onGroupSelected());

        HBox groupTitleRow = new HBox(6, groupTitle, groupEditBtn);
        groupTitleRow.setAlignment(Pos.CENTER_LEFT);

        HBox groupBoxRow = new HBox(6, detailGroupBox, groupRemoveBtn);
        groupBoxRow.setAlignment(Pos.CENTER_LEFT);

        Label addressTitle = new Label("Адрес");
        addressTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        Label noteTitle = new Label("Заметка");
        noteTitle.setFont(Font.font("System", FontWeight.BOLD, 13));

        detailPanel.getChildren().addAll(
                nameRow, new Separator(),
                phonesTitle, detailPhones,
                emailTitle, detailEmail,
                groupTitleRow, groupBoxRow,
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
        Button exportBtn = new Button("Экспорт");
        exportBtn.setOnAction(e -> onExport());

        Button importBtn = new Button("Импорт");
        importBtn.setOnAction(e -> onImport());

        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> refresh());

        HBox box = new HBox(10, exportBtn, importBtn, refreshBtn);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    private void refresh() {
        log("refresh: start");
        String query = searchField.getText();
        List<Contact> contacts = contactService.search(query);
        log("refresh: loaded contacts = " + contacts.size());

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
        log("refresh: allRows filled = " + allRows.size());
        applyFilter();
        log("refresh: end");
    }

    private void applyFilter() {
        log("applyFilter: start, phonesMode=" + phonesMode);
        if (phonesMode) {
            visibleRows.setAll(allRows);
            log("applyFilter: set allRows, size=" + allRows.size());
        } else {
            List<PhoneRow> primary = new ArrayList<>();
            for (PhoneRow row : allRows) {
                if (row.isPrimary() || row.getPhone() == null) {
                    primary.add(row);
                }
            }
            visibleRows.setAll(primary);
            log("applyFilter: set primary, size=" + primary.size());
        }
        log("applyFilter: end");
    }

    private void showDetails(Contact c) {
        log("showDetails: start, contact=" + (c == null ? "null" : c.getFullName()));
        detailPhones.getChildren().clear();

        boolean hasSelection = (c != null);
        detailCopyBtn.setDisable(!hasSelection);
        detailEditBtn.setDisable(!hasSelection);
        detailDeleteBtn.setDisable(!hasSelection);
        groupEditBtn.setDisable(!hasSelection);
        groupRemoveBtn.setDisable(!hasSelection || c == null || c.getGroupId() == null);
        detailGroupBox.setDisable(!hasSelection);

        if (c == null) {
            detailName.setText("Выберите контакт");
            detailEmail.setText("");
            suppressGroupAction = true;
            detailGroupBox.setItems(FXCollections.observableArrayList());
            detailGroupBox.setValue(null);
            suppressGroupAction = false;
            detailAddress.setText("");
            detailNote.setText("");
            return;
        }

        detailName.setText(c.getFullName());
        if (c.getPhones().isEmpty()) {
            detailPhones.getChildren().add(new Label("— нет телефонов —"));
        } else {
            for (Phone p : c.getPhones()) {
                detailPhones.getChildren().add(buildPhoneRow(p));
            }
        }
        detailEmail.setText(c.getEmail() != null ? c.getEmail() : "—");

        List<Group> groups = new ArrayList<>(groupService.findAll());
        groups.add(newGroupPlaceholder());

        suppressGroupAction = true;
        detailGroupBox.setItems(FXCollections.observableArrayList(groups));

        Group current = null;
        if (c.getGroupId() != null) {
            current = groups.stream()
                    .filter(g -> g.getId().equals(c.getGroupId()))
                    .findFirst()
                    .orElse(null);
        }
        detailGroupBox.setValue(current);
        suppressGroupAction = false;

        detailAddress.setText(c.getAddress() != null ? c.getAddress() : "—");
        detailNote.setText(c.getNote() != null ? c.getNote() : "—");
        log("showDetails: end");
    }

    private HBox buildPhoneRow(Phone phone) {
        Label label = new Label("• " + phone);

        Button copyBtn = new Button("📋");
        copyBtn.setTooltip(new Tooltip("Копировать номер"));
        copyBtn.setStyle("-fx-font-size: 11; -fx-padding: 1 4 1 4;");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(phone.getNumber());
            clipboard.setContent(content);
        });

        Button callBtn = new Button("📞");
        callBtn.setTooltip(new Tooltip("Позвонить"));
        callBtn.setStyle("-fx-font-size: 11; -fx-padding: 1 4 1 4;");
        callBtn.setOnAction(e -> onCall(phone.getNumber()));

        HBox row = new HBox(6, label, copyBtn, callBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void onCall(String number) {
        if (number == null || number.isBlank()) return;

        String clean = number.replaceAll("[^0-9+]", "");
        String uri = "tel:" + clean;

        try {
            if (!java.awt.Desktop.isDesktopSupported()) {
                showError("Система не поддерживает открытие внешних приложений");
                return;
            }
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(uri));
        } catch (Exception e) {
            showError("Не удалось открыть приложение для звонка: " + e.getMessage());
        }
    }

    private void onGroupSelected() {
        if (suppressGroupAction) return;
        Contact selected = selectedContact();
        if (selected == null) return;

        Group chosen = detailGroupBox.getValue();

        if (chosen != null && chosen.getId() == -1L) {
            onAddGroupCommand();
            return;
        }

        Long newGroupId = chosen != null ? chosen.getId() : null;

        if (java.util.Objects.equals(selected.getGroupId(), newGroupId)) return;

        selected.setGroupId(newGroupId);
        contactService.update(selected);
        selectRowForContact(selected.getId());
    }

    private void onAddGroupCommand() {
        Contact selected = selectedContact();
        if (selected == null) return;

        Long oldGroupId = selected.getGroupId();

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Новая группа");
        dialog.setHeaderText(null);
        dialog.setContentText("Название:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty() || result.get().isBlank()) {
            restoreGroupSelection(oldGroupId);
            return;
        }

        try {
            Group created = groupService.create(result.get(), null);
            selected.setGroupId(created.getId());
            contactService.update(selected);

            suppressGroupAction = true;
            List<Group> groups = new ArrayList<>(groupService.findAll());
            groups.add(newGroupPlaceholder());
            detailGroupBox.setItems(FXCollections.observableArrayList(groups));
            detailGroupBox.setValue(created);
            suppressGroupAction = false;

            selectRowForContact(selected.getId());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            restoreGroupSelection(oldGroupId);
        }
    }

    private void restoreGroupSelection(Long groupId) {
        suppressGroupAction = true;
        if (groupId == null) {
            detailGroupBox.setValue(null);
        } else {
            detailGroupBox.getItems().stream()
                    .filter(g -> groupId.equals(g.getId()))
                    .findFirst()
                    .ifPresent(detailGroupBox::setValue);
        }
        suppressGroupAction = false;
    }

    private Contact selectedContact() {
        PhoneRow row = table.getSelectionModel().getSelectedItem();
        return row != null ? row.getContact() : null;
    }

    private void onRemoveGroupFromContact() {
        log("onRemoveGroupFromContact: start");
        Contact selected = selectedContact();
        if (selected == null || selected.getGroupId() == null) {
            log("onRemoveGroupFromContact: exit");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Убрать группу");
        confirm.setHeaderText(null);
        confirm.setContentText("Убрать группу у контакта \"" + selected.getFullName() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                selected.setGroupId(null);
                contactService.update(selected);

                suppressGroupAction = true;
                detailGroupBox.setValue(null);
                suppressGroupAction = false;

                selectRowForContact(selected.getId());

                log("onRemoveGroupFromContact: end");
            }
        });
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
        Long contactId = selected.getId();

        ContactForm form = new ContactForm(selected);
        form.showAndWait();

        refresh();
        selectRowForContact(contactId);
    }

    private void onDelete() {
        Contact selected = selectedContact();
        if (selected == null) {
            showError("Выберите контакт");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление контакта");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить контакт \"" + selected.getFullName() + "\"?\nВсе его телефоны тоже будут удалены.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                contactService.delete(selected.getId());
                refresh();
            }
        });
    }

    private void onGroups() {
        Contact selected = selectedContact();
        Long contactId = selected != null ? selected.getId() : null;

        GroupManager manager = new GroupManager();
        manager.showAndWait();

        refresh();
        selectRowForContact(contactId);
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

    private void selectRowForContact(Long contactId) {
        if (contactId == null) return;
        for (PhoneRow row : visibleRows) {
            if (row.getContact().getId().equals(contactId)) {
                table.getSelectionModel().select(row);
                table.requestFocus();
                return;
            }
        }
    }

    private Group newGroupPlaceholder() {
        Group g = new Group();
        g.setId(-1L);
        g.setName("+ Новая группа...");
        return g;
    }

    private void onCopyCard() {
        Contact selected = selectedContact();
        if (selected == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append(selected.getFullName()).append("\n");

        if (!selected.getPhones().isEmpty()) {
            sb.append("Телефоны:\n");
            for (Phone p : selected.getPhones()) {
                sb.append(p.getNumber());
                if (p.getType() != null && !p.getType().isBlank()) {
                    sb.append(" (").append(p.getType()).append(")");
                }
                sb.append("\n");
            }
        }

        if (selected.getEmail() != null && !selected.getEmail().isBlank()) {
            sb.append("Email: ").append(selected.getEmail()).append("\n");
        }
        if (selected.getGroupId() != null) {
            sb.append("Группа: ").append(resolveGroupName(selected.getGroupId())).append("\n");
        }
        if (selected.getAddress() != null && !selected.getAddress().isBlank()) {
            sb.append("Адрес: ").append(selected.getAddress()).append("\n");
        }
        if (selected.getNote() != null && !selected.getNote().isBlank()) {
            sb.append("Заметка: ").append(selected.getNote()).append("\n");
        }

        javafx.scene.input.Clipboard clipboard = Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString().trim());
        clipboard.setContent(content);
    }

    private String resolveGroupName(Long groupId) {
        return groupService.findAll().stream()
                .filter(g -> g.getId().equals(groupId))
                .map(Group::getName)
                .findFirst()
                .orElse("—");
    }

    private static final boolean DEBUG = false;

    private void log(String message) {
        if (DEBUG) {
            System.out.println("[" + LocalTime.now().withNano(0) + "] " + message);
        }
    }
}