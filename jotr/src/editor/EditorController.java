package editor;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static javafx.scene.input.KeyCode.*;

public class EditorController {

    private final EditorModel model;

    @FXML
    private TextArea textArea;

    private TextFile currentTextFile;

    private FileChooser fileChooser;

    private Stage finderStage;

    @FXML
    private HBox statusBar;

    @FXML
    private Label caretLabel;

    @FXML
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
        // initialize current file to null values
        currentTextFile = new TextFile(null, Collections.singletonList(""));

        //initialize file chooser to keep track of last location used
        fileChooser = new FileChooser();

        //TODO: Find a way to get Line and Column
        caretLabel.textProperty().bind(textArea.caretPositionProperty().asString("Pos %d :("));

        //initialize and set limits to zoom property
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

        //----------Key Combinations----------//

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


    /* * * * * * * *\
     *  FILE MENU *
    \* * * * * * * */

    @FXML
    private void onNew() {
        if (hasNoTextAreaChanges() || savePrompt()) {
            textArea.clear();
            //reinitialize current text file
            currentTextFile = new TextFile(null, Collections.singletonList(""));
        }
    }

    @FXML
    private void onNewWindow() {
        //TODO: Find a way to make it a separate process
        Platform.runLater(() -> {
            try {
                new EditorMain().start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void onOpen() {
        if (hasNoTextAreaChanges() || savePrompt()) {
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
                    fileChooser.setInitialDirectory(currentTextFile.getFile().getParent().toFile());
                } else {
                    System.out.println("Failed");
                }
            }
        }
    }

    @FXML
    private boolean onSave() {
        boolean fileWasSaved = true;
        try {
            currentTextFile = new TextFile(currentTextFile.getFile(), getTextAreaAsList());
            model.save(currentTextFile);
        } catch (Exception e) {
            fileWasSaved = false;
            onSaveAs();
        }
        return fileWasSaved;
    }

    @FXML
    private boolean onSaveAs() {
        boolean fileWasChosen = true;
        try {
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Documents", "*.txt"),
                    new FileChooser.ExtensionFilter("All Files", "*.*"));
            File file = fileChooser.showSaveDialog(null);

            currentTextFile = new TextFile(file.toPath(), getTextAreaAsList());
            model.save(currentTextFile);

            fileChooser.setInitialDirectory(currentTextFile.getFile().getParent().toFile());
        } catch (NullPointerException e) {
            System.out.println("No file was selected!");
            fileWasChosen = false;
        }
        return fileWasChosen;
    }

    @FXML
    public void onExit() {
        if (hasNoTextAreaChanges() || savePrompt())
            model.exit();
    }

    // Save Prompt

    private List<String> getTextAreaAsList() {
        return Arrays.asList(textArea.getText().split("\n"));
    }

    private boolean hasNoTextAreaChanges() {
        return getTextAreaAsList().equals(currentTextFile.getContent());
    }

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

        // change window icon and show alert
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));
        alert.initOwner(stage.getOwner());
        Optional<ButtonType> result = alert.showAndWait();

        // interpret user choice
        if (result.isPresent() && result.get().equals(alert.getButtonTypes().get(0))) {
                if (currentTextFile.getFile() == null)
                    return onSaveAs();
                else return onSave();
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

    @FXML
    private void onFind() throws Exception {
        openFinder(FinderMode.FIND);
    }

    //TODO: close find window when replace is opened

    @FXML
    private void onReplace() throws Exception {
        if (finderStage != null) finderStage.close();
        openFinder(FinderMode.REPLACE);
    }

    private void openFinder(FinderMode mode) throws Exception {
        if (finderStage != null) finderStage.close();
        finderStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("finderUI.fxml"));
        loader.setControllerFactory(t -> new FinderController(finderStage, textArea, mode));
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
    private void onSelectAll() {
        textArea.selectAll();
    }


    /* * * * * * * *\
     *  VIEW MENU  *
    \* * * * * * * */

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

    // change zoom

    private void changeZoom(int sizeChange) {
        zoom.setValue(zoom.intValue() + sizeChange);
        double newSize = (int) (defaultFont.getSize() * zoom.floatValue() / 100f);
        textArea.setFont(new Font(defaultFont.getName(), newSize));
        // fix for caret visual glitch
        int caretPos = textArea.getCaretPosition();
        textArea.positionCaret(0);
        textArea.positionCaret(caretPos);
    }


    /* * * * * * * *\
     *  HELP MENU  *
    \* * * * * * * */

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("About Memobook");
        alert.setContentText("A simple text editor for my final project.");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icon.png")));
        stage.show();
    }


    // STATUS BAR

}
