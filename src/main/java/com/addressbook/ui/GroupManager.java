package com.addressbook.ui;

import com.addressbook.model.Group;
import com.addressbook.service.GroupService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Диалог управления группами: список, добавление, переименование, удаление.
 */
public class GroupManager extends Dialog<Void> {

    private final GroupService groupService = new GroupService();
    private final ObservableList<Group> groups = FXCollections.observableArrayList();
    private final ListView<Group> listView = new ListView<>(groups);

    public GroupManager() {
        setTitle("Управление группами");
        setHeaderText(null);

        ButtonType closeType = new ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().add(closeType);
        getDialogPane().setPrefWidth(450);

        getDialogPane().setContent(buildContent());
        refresh();
    }

    private VBox buildContent() {
        listView.setPrefHeight(250);
        listView.setPlaceholder(new Label("Групп пока нет"));

        Button addBtn = new Button("Добавить");
        addBtn.setOnAction(e -> onAdd());

        Button renameBtn = new Button("Переименовать");
        renameBtn.setOnAction(e -> onRename());

        Button deleteBtn = new Button("Удалить");
        deleteBtn.setOnAction(e -> onDelete());

        HBox buttons = new HBox(10, addBtn, renameBtn, deleteBtn);

        VBox root = new VBox(10, new Label("Группы:"), listView, buttons);
        root.setPadding(new Insets(15));
        return root;
    }

    private void refresh() {
        groups.setAll(groupService.findAll());
    }

    private void onAdd() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Новая группа");
        dialog.setHeaderText(null);
        dialog.setContentText("Название:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                groupService.create(name, null);
                refresh();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        });
    }

    private void onRename() {
        Group selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите группу");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Переименовать группу");
        dialog.setHeaderText(null);
        dialog.setContentText("Новое название:");
        dialog.showAndWait().ifPresent(name -> {
            try {
                selected.setName(name);
                groupService.update(selected);
                refresh();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        });
    }

    private void onDelete() {
        Group selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Выберите группу");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление");
        confirm.setHeaderText(null);
        confirm.setContentText("Удалить группу \"" + selected.getName() + "\"?\nКонтакты останутся, но без группы.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                groupService.delete(selected.getId());
                refresh();
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}