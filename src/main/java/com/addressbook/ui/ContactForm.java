package com.addressbook.ui;

import com.addressbook.model.Contact;
import com.addressbook.model.Group;
import com.addressbook.model.Phone;
import com.addressbook.service.ContactService;
import com.addressbook.service.GroupService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Диалог добавления и редактирования контакта.
 * Телефоны редактируются как список полей ввода.
 */
public class ContactForm extends Dialog<Contact> {

    private final ContactService contactService = new ContactService();
    private final GroupService groupService = new GroupService();

    private final TextField lastNameField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField middleNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField addressField = new TextField();
    private final TextArea noteArea = new TextArea();
    private final ComboBox<Group> groupBox = new ComboBox<>();

    private final VBox phonesBox = new VBox(6);
    private final List<PhoneRow> phoneRows = new ArrayList<>();

    private final Contact contact;

    public ContactForm(Contact contact) {
        this.contact = contact != null ? contact : new Contact();

        setTitle(contact == null ? "Новый контакт" : "Редактирование контакта");
        setHeaderText(null);

        ButtonType saveType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveType, cancelType);
        getDialogPane().setPrefWidth(600);

        getDialogPane().setContent(buildForm());
        fillFields();

        setResultConverter(button -> button == saveType ? onSave() : null);
    }

    private VBox buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        int row = 0;
        grid.add(new Label("Фамилия *:"), 0, row);
        grid.add(lastNameField, 1, row++);

        grid.add(new Label("Имя *:"), 0, row);
        grid.add(firstNameField, 1, row++);

        grid.add(new Label("Отчество:"), 0, row);
        grid.add(middleNameField, 1, row++);

        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);

        grid.add(new Label("Адрес:"), 0, row);
        grid.add(addressField, 1, row++);

        grid.add(new Label("Группа:"), 0, row);
        groupBox.getItems().setAll(groupService.findAll());
        groupBox.setPrefWidth(250);
        grid.add(groupBox, 1, row++);

        grid.add(new Label("Заметка:"), 0, row);
        noteArea.setPrefRowCount(2);
        noteArea.setPrefWidth(300);
        grid.add(noteArea, 1, row);

        Label phonesTitle = new Label("Телефоны:");
        phonesTitle.setStyle("-fx-font-weight: bold;");

        Button addPhoneBtn = new Button("+ Добавить телефон");
        addPhoneBtn.setOnAction(e -> addPhoneRow(new Phone("+7", "мобильный")));

        VBox phonesSection = new VBox(8, phonesTitle, phonesBox, addPhoneBtn);

        VBox root = new VBox(15, grid, phonesSection);
        root.setPadding(new Insets(15));
        return root;
    }

    private void addPhoneRow(Phone phone) {
        PhoneRow row = new PhoneRow(phone);
        phoneRows.add(row);
        phonesBox.getChildren().add(row.getNode());
    }

    private void removePhoneRow(PhoneRow row) {
        phoneRows.remove(row);
        phonesBox.getChildren().remove(row.getNode());
    }

    private void fillFields() {
        lastNameField.setText(contact.getLastName());
        firstNameField.setText(contact.getFirstName());
        middleNameField.setText(contact.getMiddleName());
        emailField.setText(contact.getEmail());
        addressField.setText(contact.getAddress());
        noteArea.setText(contact.getNote());

        if (contact.getGroupId() != null) {
            groupBox.getItems().stream()
                    .filter(g -> g.getId().equals(contact.getGroupId()))
                    .findFirst()
                    .ifPresent(groupBox::setValue);
        }

        if (contact.getPhones().isEmpty()) {
            addPhoneRow(new Phone("+7", "мобильный"));
        } else {
            for (Phone p : contact.getPhones()) {
                addPhoneRow(p);
            }
        }
    }

    private Contact onSave() {
        contact.setLastName(lastNameField.getText());
        contact.setFirstName(firstNameField.getText());
        contact.setMiddleName(middleNameField.getText());
        contact.setEmail(emailField.getText());
        contact.setAddress(addressField.getText());
        contact.setNote(noteArea.getText());

        Group selected = groupBox.getValue();
        contact.setGroupId(selected != null ? selected.getId() : null);

        List<Phone> phones = new ArrayList<>();
        for (PhoneRow row : phoneRows) {
            phones.add(row.toPhone());
        }
        contact.setPhones(phones);

        try {
            if (contact.getId() == null) {
                contactService.create(contact);
            } else {
                contactService.update(contact);
            }
            return contact;
        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось сохранить контакт");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return null;
        }
    }

    /**
     * Одна строка ввода телефона: [Номер] [Тип] [−]
     */
    private class PhoneRow {
        private final TextField numberField = new TextField();
        private final ComboBox<String> typeBox = new ComboBox<>();
        private final HBox node;

        PhoneRow(Phone phone) {
            numberField.setText(phone.getNumber());
            numberField.setPrefWidth(200);

            typeBox.getItems().addAll("мобильный", "рабочий", "домашний", "другое");
            typeBox.setValue(phone.getType() != null ? phone.getType() : "мобильный");
            typeBox.setPrefWidth(130);

            Button removeBtn = new Button("−");
            removeBtn.setOnAction(e -> removePhoneRow(this));

            node = new HBox(8, numberField, typeBox, removeBtn);
            HBox.setHgrow(numberField, Priority.ALWAYS);
        }

        Phone toPhone() {
            Phone p = new Phone();
            p.setId(null);
            p.setNumber(numberField.getText());
            p.setType(typeBox.getValue());
            return p;
        }

        HBox getNode() {
            return node;
        }
    }
}