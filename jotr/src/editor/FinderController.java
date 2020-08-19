package editor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum FinderMode {FIND, REPLACE}

public class FinderController {

    private final Stage finderStage;

    private final FinderMode mode;

    private final TextArea textArea;

    @FXML
    private TextField findField;

    private Pattern pattern;

    @FXML
    private HBox directionToggleBox;

    @FXML
    private Toggle directionDown;

    @FXML
    private Toggle directionUp;

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
    private Button replaceButton;

    @FXML
    private Button replaceAllButton;

    public FinderController(Stage stage, TextArea textArea, FinderMode mode) {
        this.finderStage = stage;
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

    /* * * * * * * *\
     *  FIND MODE  *
    \* * * * * * * */

    @FXML
    private void onFindNext() {
        int[] selectionBounds = findMatchBounds(textArea.getCaretPosition());

        if (selectionBounds[0] == -1) {
            //TODO: Make alert
            System.out.println("COULDN'T FIND " + findField.getText());
        } else {
            textArea.selectRange(selectionBounds[0], selectionBounds[1]);
        }
    }

    public int[] findMatchBounds(int startIndex) {
        int[] bounds = new int[] {-1, 0};
        String textAreaContent = textArea.getText();
        String findFieldText = findField.getText();
        int flags = 0;

        //TODO: Find a better way to search backwards
        if (directionUp.isSelected()) {
            int endIndex = startIndex <= -1 ? startIndex * -1 : textArea.getSelection().getStart();
            startIndex = 0;
            textAreaContent = textAreaContent.substring(startIndex, endIndex);
            if (!regExCheckBox.isSelected()) {
                if (!matchCaseCheckBox.isSelected()) {
                    textAreaContent = textAreaContent.toLowerCase();
                    findFieldText = findFieldText.toLowerCase();
                }
                startIndex = textAreaContent.lastIndexOf(findFieldText);
                startIndex = startIndex == -1 ? 0 : startIndex;
            } else {
                findFieldText += "(?!.*" + findFieldText + ")";
            }
        }

        if (!matchCaseCheckBox.isSelected())
            flags = flags | Pattern.CASE_INSENSITIVE;

        if (!regExCheckBox.isSelected())
            flags = flags | Pattern.LITERAL;

        pattern = Pattern.compile(findFieldText, flags);

        Matcher matcher = pattern.matcher(textAreaContent);

        if(matcher.find(startIndex)) {
            bounds[0] = matcher.start();
            bounds[1] = matcher.end();

        } else if (wrapAroundCheckBox.isSelected()) {
            Matcher newMatcher = pattern.matcher(textArea.getText());

            if (newMatcher.find()) {
                if (directionDown.isSelected())
                    bounds = findMatchBounds(0);
                else
                    bounds = findMatchBounds(textArea.getText().length() * -1);
            }
        }

        return bounds;
    }

    @FXML
    private void onCancel() {
        finderStage.close();
    }

    /* * * * * * * * *\
     *  REPLACE MODE *
    \* * * * * * * * */

    @FXML
    private void onReplace() {
        if (textArea.selectedTextProperty().length().intValue() == 0) {
            onFindNext();
        } else {
            String textToReplace = textArea.getSelectedText();
            Matcher matcher = pattern.matcher(textToReplace);
            String textReplacement = matcher.replaceFirst(replaceField.getText());

            textArea.replaceSelection(textReplacement);
            onFindNext();
        }
    }

    @FXML
    private void onReplaceAll() {
        findMatchBounds(0);
        String textToReplace = textArea.getText();
        Matcher matcher = pattern.matcher(textToReplace);
        String textReplacement = matcher.replaceAll(replaceField.getText());

        textArea.replaceText(0, textArea.getLength(), textReplacement);
    }



}



