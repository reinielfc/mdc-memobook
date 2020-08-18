package editor;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.*;

import java.io.File;
import java.io.FileDescriptor;
import java.util.*;

import static javafx.scene.input.KeyCode.*;

public class EditorController {

    @FXML
    private TextArea textArea;

    private TextFile currentTextFile;

    private final EditorModel model;

    @FXML
    private HBox statusBar;

    private Font defaultFont;

    @FXML
    private Label zoomLabel;

    private SimpleIntegerProperty zoom;

    @FXML
    private MenuItem
            newCmd, newWindowCmd, openCmd, saveCmd, saveAsCmd, exitCmd,
            undoCmd, cutCmd, copyCmd, pasteCmd, deleteCmd,
            findCmd, findNextCmd, findPreviousCmd, replaceCmd, goToCmd, selectAllCmd,
            zoomInCmd, zoomOutCmd, restoreZoomCmd;

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

        // Key Combinations

        //file menu
        newCmd.setAccelerator(new KeyCodeCombination(N, KeyCombination.CONTROL_DOWN));
        newWindowCmd.setAccelerator(new KeyCodeCombination(N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        openCmd.setAccelerator(new KeyCodeCombination(O, KeyCombination.CONTROL_DOWN));
        saveCmd.setAccelerator(new KeyCodeCombination(S, KeyCombination.CONTROL_DOWN));
        saveAsCmd.setAccelerator(new KeyCodeCombination(S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        exitCmd.setAccelerator(new KeyCodeCombination(E, KeyCombination.CONTROL_DOWN));

        //edit menu
        undoCmd.setAccelerator(new KeyCodeCombination(Z, KeyCombination.CONTROL_DOWN));
        cutCmd.setAccelerator(new KeyCodeCombination(X, KeyCombination.CONTROL_DOWN));
        copyCmd.setAccelerator(new KeyCodeCombination(C, KeyCombination.CONTROL_DOWN));
        pasteCmd.setAccelerator(new KeyCodeCombination(V, KeyCombination.CONTROL_DOWN));
        deleteCmd.setAccelerator(new KeyCodeCombination(DELETE));
        findCmd.setAccelerator(new KeyCodeCombination(F, KeyCombination.CONTROL_DOWN));
        findNextCmd.setAccelerator(new KeyCodeCombination(F3));
        findPreviousCmd.setAccelerator(new KeyCodeCombination(F3, KeyCombination.SHIFT_DOWN));
        replaceCmd.setAccelerator(new KeyCodeCombination(H, KeyCombination.CONTROL_DOWN));
        goToCmd.setAccelerator(new KeyCodeCombination(G, KeyCombination.CONTROL_DOWN));
        selectAllCmd.setAccelerator(new KeyCodeCombination(A, KeyCombination.CONTROL_DOWN));

        //zoom menu
        zoomInCmd.setAccelerator(new KeyCodeCombination(ADD, KeyCombination.CONTROL_DOWN));
        zoomOutCmd.setAccelerator(new KeyCodeCombination(SUBTRACT, KeyCombination.CONTROL_DOWN));
        restoreZoomCmd.setAccelerator(new KeyCodeCombination(DIGIT0, KeyCombination.CONTROL_DOWN));


    }

    public TextArea getTextArea() {
        return textArea;
    }

    /* * * * * * * *\
     *  FILE MENU *
    \* * * * * * * */

    @FXML
    private void onNew() {
        List<String> currentTextArea = Arrays.asList(textArea.getText().split("\n"));
        if (currentTextArea.equals(currentTextFile.getContent()) || savePrompt()) {
            textArea.clear();
            //reinitialize current text file
            currentTextFile = new TextFile(null, Collections.singletonList(""));
        }
    }

    //TODO: Add onNewWindow() Method

    @FXML
    private void onOpen() {
        List<String> currentTextArea = Arrays.asList(textArea.getText().split("\n"));
        if (currentTextArea.equals(currentTextFile.getContent()) || savePrompt()) {
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
        try {
            List<String> currentTextArea = Arrays.asList(textArea.getText().split("\n"));
            currentTextFile = new TextFile(currentTextFile.getFile(), currentTextArea);
            model.save(currentTextFile);
        } catch (Exception e) {
            e.printStackTrace();
            onSaveAs();
        }
    }

    @FXML
    private void onSaveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Documents", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File file = fileChooser.showSaveDialog(null);
        List<String> currentTextArea = Arrays.asList(textArea.getText().split("\n"));
        TextFile textFile = new TextFile(file.toPath(), currentTextArea);
        model.save(textFile);
    }

    @FXML
    private void onExit() {
        List<String> currentTextArea = Arrays.asList(textArea.getText().split("\n"));
        if (currentTextArea.equals(currentTextFile.getContent()) || savePrompt())
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
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();

        content.putString(textArea.getSelectedText());
        clipboard.setContent(content);
    }

    @FXML
    private void onCut() {
        onCopy();
        textArea.deleteText(textArea.getSelection().getStart(), textArea.getSelection().getEnd());
    }

    @FXML
    private void onPaste() {
        textArea.insertText(textArea.getCaretPosition(), Clipboard.getSystemClipboard().getString());
    }

    @FXML
    private void onDelete() {
        textArea.deleteText(textArea.getSelection().getStart(), textArea.getSelection().getEnd());
    }

    //TODO: Make a GUI for onFind, finish it

    private void openFinder(FinderMode mode) throws Exception {
        Stage finderStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("finderUI.fxml"));
        loader.setControllerFactory(t -> new FinderController(textArea, mode));
        finderStage.setTitle(mode == FinderMode.FIND ? "Find" : "Replace");
        finderStage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));
        finderStage.setScene(new Scene(loader.load()));
        finderStage.resizableProperty().setValue(false);
        finderStage.initOwner(EditorMain.getPrimaryWindow());
        finderStage.initStyle(StageStyle.UNIFIED);
        finderStage.initModality(Modality.NONE);
        finderStage.show();
    }

    @FXML
    private void onFind() throws Exception {
        openFinder(FinderMode.FIND);
    }

    @FXML
    private void onReplace() throws Exception {
        openFinder(FinderMode.REPLACE);
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
