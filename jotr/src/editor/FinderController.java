package editor;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum FinderMode {FIND, REPLACE}

public class FinderController {

    FinderMode mode;

    private final TextArea textArea;

    private String textAreaContents;

    @FXML
    private TextField findField;

    private String textToFind;

    @FXML
    private HBox directionToggleBox;

    @FXML
    private ToggleGroup directionToggle;

    @FXML
    private CheckBox regExCheckBox;

    @FXML
    public CheckBox matchCaseCheckBox;

    @FXML
    private CheckBox wrapAroundCheckBox;

    @FXML
    private HBox replaceFieldBox;

    @FXML
    private TextField replaceField;

    @FXML
    private Button findNextButton;

    @FXML
    private Button replaceButton;

    @FXML
    private Button replaceAllButton;

    //@FXML
    //private ButtonType cancelButton = ButtonType.CANCEL;

    public FinderController(TextArea textArea, FinderMode mode) {
        this.textArea = textArea;
        this.mode = mode;
    }
    
    public void initialize() {
        if (mode == FinderMode.FIND) {
            replaceFieldBox.setVisible(false);
            replaceFieldBox.managedProperty().setValue(false);
            replaceButton.setVisible(false);
            replaceButton.managedProperty().setValue(false);
            replaceAllButton.setVisible(false);
            replaceAllButton.managedProperty().setValue(false);

        } else {
            directionToggleBox.setVisible(false);
            directionToggleBox.managedProperty().setValue(false);
        }

    }

    // FIND MODE

    //TODO: Merge regex mode with normal find mode
    public int[] findWordBounds() {
        int caretPos = textArea.getCaretPosition();
        int[] bounds = new int[2];

        if (directionToggle.getToggles().get(1).isSelected()) {
            bounds[0] = textAreaContents.indexOf(textToFind, caretPos);

            if (wrapAroundCheckBox.isSelected() && bounds[0] == -1) {
                textArea.selectRange(0, 0);
                bounds = findWordBounds();
            }

        } else {
            bounds[0] = textAreaContents
                    .substring(0, caretPos - textArea.selectedTextProperty().get().length())
                    .lastIndexOf(textToFind);

            if (wrapAroundCheckBox.isSelected() && bounds[0] == -1) {
                textArea.selectRange(textAreaContents.length(), textAreaContents.length());
                bounds = findWordBounds();
            }
        }

        bounds[1] = bounds[0] + textToFind.length();

        return bounds;
    }

    public int[] regexFindWordBounds() {
        int caretPos = textArea.getCaretPosition();
        int[] bounds = new int[] {-1, 0};

        if (directionToggle.getToggles().get(1).isSelected()) {
            Pattern pattern = Pattern.compile(textToFind);
            Matcher matcher = pattern.matcher(textAreaContents.substring(caretPos));
            if (matcher.find()) {
                bounds[0] = matcher.start() + caretPos;
                bounds[1] = matcher.end() + caretPos;
            } else if (wrapAroundCheckBox.isSelected()) {
                textArea.selectRange(0, 0);
                bounds = regexFindWordBounds();
            }
        } else {
            // TODO: this doesn't work right
            Pattern pattern = Pattern.compile("(?:.*)" + textToFind);
            Matcher matcher = pattern.matcher(textAreaContents.substring(0, caretPos));
            if (matcher.find()) {
                bounds[0] = matcher.start() - caretPos;
                bounds[1] = matcher.end() - caretPos;
            } else if (wrapAroundCheckBox.isSelected()) {
                textArea.selectRange(textAreaContents.length(), textAreaContents.length());
                //bounds = regexFindWordBounds();
            }

        }

        return bounds;
    }

    @FXML
    public void onFindNext() {
        textAreaContents = textArea.getText();
        textToFind = findField.getText();
        int[] regexBounds;

        if (matchCaseCheckBox.isSelected()) {
            textAreaContents = textAreaContents.toLowerCase();
            textToFind = textToFind.toLowerCase();
        }

        if (regExCheckBox.isSelected())
            regexBounds = regexFindWordBounds();
        else regexBounds = findWordBounds();

        if (regexBounds[0] == -1)
            System.out.println(textToFind + " <-- NOT FOUND");
        else textArea.selectRange(regexBounds[0], regexBounds[1]);
    }
    /*
        // replace selection
    System.out.print("Replace with: ");
    textArea.replaceSelection(scan.next());
    */
}



