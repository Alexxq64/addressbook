package com.addressbook.ui;

import com.addressbook.dao.DatabaseManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseManager.getInstance().initSchema();

        MainWindow window = new MainWindow();
        Scene scene = new Scene(window, 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm()
        );

        stage.setTitle("Телефонная адресная книга");
        stage.setScene(scene);
        stage.show();
    }
}