package com.reiprojects.notepad4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EditorMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("ui.fxml"));
        //Image appIcon = new Image(getClass().getResourceAsStream("/resources/icon.png"));
        //primaryStage.getIcons().add(appIcon);
        primaryStage.setTitle("Jotr");
        primaryStage.setScene(new Scene(root, 600, 354));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
