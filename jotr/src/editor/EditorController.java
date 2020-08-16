package editor;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EditorController {

    @FXML
    private EditorTextArea textArea;

    private TextFile currentTextFile;

    private final EditorModel model;

    @FXML
    private HBox statusBar;

    private Font defaultFont;

    @FXML
    private Label zoomLabel;

    private SimpleIntegerProperty zoom;

    // Constructor

    public EditorController(EditorModel model) {
        this.model = model;
    }

    // Initializer

    public void initialize() {
        //initialize current text file to default values
        currentTextFile = new TextFile(null, Collections.singletonList(""));

        //initialize "zoom"
        defaultFont = new Font(12.0);

        //set limits to zoom property
        zoom = new SimpleIntegerProperty(this, "zoom") {
            @Override
            public void setValue(Number number) {
                if (number.intValue() >= 10 && number.intValue() <= 500)
                    super.setValue(number);
            }
        };
        //set zoom value to 100 and bind it to zoomLabel
        zoom.setValue(100);
        zoomLabel.textProperty().bind(zoom.asString("%d%%"));
    }

    // FILE MENU

    @FXML
    private void onNew() {
        if (!textArea.hasUnsavedChanges(currentTextFile.getContent()) || savePrompt()) {
            textArea.clear();
            currentTextFile = new TextFile(null, Collections.singletonList(""));
        }
    }

    //TODO: Add onNewWindow Method

    @FXML
    private void onOpen() {
        if (!textArea.hasUnsavedChanges(currentTextFile.getContent()) || savePrompt()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Documents", "*.txt"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                IOResult<TextFile> io = model.open(file.toPath());

                if (io.isOk() && io.hasData()) {
                    currentTextFile = io.getData();
                    textArea.clear();
                    currentTextFile.getContent().forEach(line -> textArea.appendText(line + "\n"));
                } else {
                    System.out.println("Failed");
                }
            }
        }
    }

    @FXML
    private void onSave() {
        if (currentTextFile.getFile() == null) {
            onSaveAs();
        } else {
            TextFile textFile = new TextFile(currentTextFile.getFile(), textArea.getTextAsList());
            model.save(textFile);
        }
    }

    @FXML
    private void onSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Documents", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File file = fileChooser.showSaveDialog(null);
        TextFile textFile = new TextFile(file.toPath(), textArea.getTextAsList());
        model.save(textFile);
    }

    @FXML
    private void onExit() {
        if (!textArea.hasUnsavedChanges(currentTextFile.getContent()) || savePrompt())
            model.exit();
    }

    // Save Prompt

    private boolean savePrompt() {
        // create alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Jotr");
        alert.setHeaderText("Do you want to save changes to " +
                currentTextFile.getFileName().getValue() + "?");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(
                new ButtonType("Save"),
                new ButtonType("Don't Save"),
                new ButtonType("Cancel")
        );

        // change window icon
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));
        alert.initOwner(stage.getOwner());

        // read and interpret user input
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get().equals(alert.getButtonTypes().get(0))) {
            try {
                if (currentTextFile.getFile() == null) onSaveAs();
                else onSave();
                return true;
            } catch (NullPointerException e) {
                return false;
            }
        }
        return result.isPresent() && result.get().equals(alert.getButtonTypes().get(1));
    }

    // EDIT MENU

    @FXML
    private void onCopy() {
        model.copy(textArea.getSelectedText());
    }

    @FXML
    private void onCut() {
        onCopy();
        textArea.deleteText(textArea.getSelection().getStart(), textArea.getSelection().getEnd());
    }

    @FXML
    private void onPaste() {
        textArea.insertText(textArea.getCaretPosition(), model.paste());
    }

    @FXML
    private void onDelete() {
        textArea.deleteText(textArea.getSelection().getStart(), textArea.getSelection().getEnd());
    }

    @FXML
    private void onSelectAll() {
        textArea.selectAll();
    }

    // VIEW MENU

    private void changeZoom(int sizeChange) {
        zoom.setValue(zoom.intValue() + sizeChange);
        double newSize = (int) (defaultFont.getSize() * zoom.floatValue() / 100f);
        textArea.setFont(new Font(defaultFont.getFamily(), newSize));
    }

    @FXML
    private void onZoomIn() {
        changeZoom(10);
    }

    @FXML
    private void onZoomOut() {
        changeZoom(-10);
    }

    @FXML
    private void onRestoreZoom() {
        changeZoom(100 - zoom.intValue());
    }

    @FXML
    private void onWordWrap() {
        textArea.setWrapText(!textArea.isWrapText());
    }

    @FXML
    private void onStatusBar() {
        statusBar.setVisible(!statusBar.isVisible());
        statusBar.managedProperty().bind(statusBar.visibleProperty());
    }

    // HELP MENU

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("About JotR");
        alert.setContentText("A simple text editor for my final project.");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        //TODO: Try to make it so no icon shows up at all
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));
        stage.show();
    }

    // STATUS BAR

}
