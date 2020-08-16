package com.reiprojects.notepad3;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;

public class EditorController {

    @FXML
    private TextArea textArea;

    @FXML
    private HBox statusBar;

    // File Menu

    @FXML
    private void onNew() {}

    @FXML
    private void onOpen() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
    }

    @FXML
    private void onSave() {}

    @FXML
    private void onSaveAs() {}

    @FXML
    private void onExit() {
        System.exit(0);
    }

    // Edit Menu

    // View Menu

    private void onZoom(double sizeChange){
        Font font = textArea.getFont();
        double newSize = font.getSize() + sizeChange;
        textArea.setFont(new Font(font.getFamily(), newSize));
    }

    @FXML
    private void onZoomIn() {
        onZoom(1.0);
    }

    @FXML
    private void onZoomOut() {
        onZoom(-1.0);
    }

    @FXML
    private void onRestoreZoom() {
        textArea.setFont(new Font(12.0));
    }

    @FXML
    private void onWordWrap() {
        textArea.setWrapText(!textArea.isWrapText());
    }

    @FXML
    private void onStatusBar() {
        statusBar.setVisible(!statusBar.isVisible());
        //TODO: Make it so textArea fills the space left by hiding statusBar
    }


    // Help menu

}
