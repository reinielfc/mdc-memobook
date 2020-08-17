package editor;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class EditorModel {

    // FILE MENU

    public void save(TextFile textFile) {
        try {
            Files.write(textFile.getFile(), textFile.getContent(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IOResult<TextFile> open(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);
            return new IOResult<>(new TextFile(file, lines), true);
        } catch (IOException e) {
            e.printStackTrace();
            return new IOResult<>(null, false);
        }
    }

    public void exit() {
        System.exit(0);
    }

    // EDIT MENU

    public void copy(String text) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();

        content.putString(text);
        clipboard.setContent(content);
    }

    public String paste() {
        return Clipboard.getSystemClipboard().getString();
    }

}
