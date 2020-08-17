package editor;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.css.Match;
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
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    /* * * * * * * *\
     *  FILE MENU *
    \* * * * * * * */

    @FXML
    private void onNew() {
        if (textArea.hasNoUnsavedChanges(currentTextFile.getContent()) || savePrompt()) {
            textArea.clear();
            currentTextFile = new TextFile(null, Collections.singletonList(""));
        }
    }

    //TODO: Add onNewWindow() Method

    @FXML
    private void onOpen() {
        if (textArea.hasNoUnsavedChanges(currentTextFile.getContent()) || savePrompt()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Documents", "*.txt"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File file = fileChooser.showOpenDialog(null);
            //TODO: Find a way to set initial directory to last accessed directory
            //https://java-buddy.blogspot.com/2012/03/javafx-20-filechooser-set-initial.html
            //if (currentTextFile != null)
            //    fileChooser.setInitialDirectory(currentTextFile.getFile().toFile());
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
        if (textArea.hasNoUnsavedChanges(currentTextFile.getContent()) || savePrompt())
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


    /* * * * * * * *\
     *  EDIT MENU  *
    \* * * * * * * */

    @FXML
    private void onUndo() {
        textArea.undo();
    }

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

    //TODO: Make a GUI for onFind, finish it
    @FXML
    private void onFind() {
        Scanner scan = new Scanner(System.in);
        String text = textArea.getText();
        int i1, i2;

        // Popup menu
        System.out.print("Find what: ");
        String regex = scan.next();

        System.out.print("Direction (U/d) ");
        boolean searchUp = scan.next().toLowerCase().equals("u");

        //System.out.print("Match Case? (Y/n) ");
        //if (scan.next().toLowerCase().equals("y"))
        //    text = text.toLowerCase();

        //System.out.println("Wrap around? (Y/n) ");
        //if(scan.next().toLowerCase().equals("y"))

        //System.out.println("Regex? (Y/n)");
        //boolean isRegex = scan.next().toLowerCase().equals("y");


        //if (isRegex) {
        //    Pattern pattern = Pattern.compile(regex);
        //    Matcher matcher = pattern.matcher(text);

        //    System.out.println(matcher.find());
        //    System.out.println(matcher.start());
            //i1 = matcher.start();
            //i2 = matcher.end();

            //textArea.selectRange(i1, i2);
        //} else {
        //    System.out.println("no regex here");
        //}

        // search direction
        if (searchUp) i1 = text.lastIndexOf(regex);
        else i1 = text.indexOf(regex);

        // find pattern and select
        if (i1 == -1) System.out.println("Cannot find \"" + regex + "\""); // popup
        else textArea.selectRange(i1, i1 + regex.length());

        // replace selection
        System.out.print("Replace with: ");
        textArea.replaceSelection(scan.next());
    }

    @FXML
    private void onSelectAll() {
        textArea.selectAll();
    }


    /* * * * * * * *\
     *  VIEW MENU  *
    \* * * * * * * */

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


    /* * * * * * * *\
     *  HELP MENU  *
    \* * * * * * * */

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
