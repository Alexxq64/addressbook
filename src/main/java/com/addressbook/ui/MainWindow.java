package com.addressbook.ui;

import com.addressbook.model.Contact;
import com.addressbook.model.Phone;
import com.addressbook.service.ContactService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.addressbook.model.Group;
import com.addressbook.service.GroupService;

/**
 * Главное окно: слева список контактов, справа — детали выбранного.
 */
public class MainWindow extends BorderPane {

    private final ContactService contactService = new ContactService();
    private final GroupService groupService = new GroupService();
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();

    private final TextField searchField = new TextField();
    private final TableView<Contact> table = new TableView<>();

    // Панель деталей
    private final Label detailName = new Label();
    private final VBox detailPhones = new VBox(5);
    private final Label detailEmail = new Label();
    private final Label detailAddress = new Label();
    private final Label detailNote = new Label();
    private final VBox detailPanel = new VBox(10);
    private final Label detailGroup = new Label();

    public MainWindow() {
        setPadding(new Insets(10));
        setTop(buildTopPanel());
        setCenter(buildCenter());
        setBottom(buildBottomPanel());
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

        HBox box = new HBox(10, new Label("Поиск:"), searchField, searchBtn, clearBtn, addBtn);
        box.setPadding(new Insets(0, 0, 10, 0));
        return box;
    }

    private SplitPane buildCenter() {
        SplitPane split = new SplitPane();
        split.getItems().addAll(buildTablePane(), buildDetailPane());
        split.setDividerPositions(0.5);
        return split;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTablePane() {
        TableColumn<Contact, String> colLast = new TableColumn<>("Фамилия");
        colLast.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLastName()));
        colLast.setPrefWidth(140);

        TableColumn<Contact, String> colFirst = new TableColumn<>("Имя");
        colFirst.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFirstName()));
        colFirst.setPrefWidth(110);

        TableColumn<Contact, String> colPhones = new TableColumn<>("Телефоны");
        colPhones.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getPhones().size())));
        colPhones.setPrefWidth(90);

        table.getColumns().addAll(colLast, colFirst, colPhones);
        table.setItems(contacts);
        table.setPlaceholder(new Label("Контактов пока нет"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> showDetails(selected));

        VBox box = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
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
                detailName,
                new Separator(),
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

        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> refresh());

        HBox box = new HBox(10, editBtn, deleteBtn, groupsBtn, refreshBtn);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    private void onGroups() {
        GroupManager manager = new GroupManager();
        manager.showAndWait();
        refresh();
    }

    private void refresh() {
        String query = searchField.getText();
        contacts.setAll(contactService.search(query));
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

    private void onAdd() {
        ContactForm form = new ContactForm(null);
        form.showAndWait().ifPresent(c -> refresh());
    }

    private void onEdit() {
        Contact selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Внимание");
            alert.setHeaderText(null);
            alert.setContentText("Выберите контакт для редактирования");
            alert.showAndWait();
            return;
        }
        ContactForm form = new ContactForm(selected);
        form.showAndWait().ifPresent(c -> refresh());
    }

    private void onDelete() {
        Contact selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Внимание");
            alert.setHeaderText(null);
            alert.setContentText("Выберите контакт для удаления");
            alert.showAndWait();
            return;
        }
        contactService.delete(selected.getId());
        refresh();
    }
}