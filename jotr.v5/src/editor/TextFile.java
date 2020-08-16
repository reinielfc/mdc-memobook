package editor;

import javafx.beans.property.SimpleStringProperty;

import java.nio.file.Path;
import java.util.List;

public class TextFile {
    private final Path file;
    private final List<String> content;
    private final SimpleStringProperty fileName;

    public TextFile(Path file, List<String> content) {
        this.file = file;
        this.content = content;

        fileName = new SimpleStringProperty(
                file == null ? "Untitled" : file.getFileName().toString());
    }

    public Path getFile() {
        return file;
    }

    public List<String> getContent() {
        return content;
    }

    public SimpleStringProperty getFileName() {
        return fileName;
    }
}

