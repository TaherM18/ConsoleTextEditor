package editor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TextEditor {
    private static final String INTERNAL_NEWLINE = "\n";

    private final Path filePath;
    private String content;
    private int cursorPosition;
    private int selectionStartPosition;
    private int selectionEndPosition;

    public TextEditor(String filePath) throws IOException {
        this.filePath = Path.of(filePath);
        initializeFile();
        this.content = loadContentFromFile();
        this.cursorPosition = this.content.length();
        this.selectionStartPosition = 0;
        this.selectionEndPosition = 0;
    }

    private void initializeFile() throws IOException {
        Path parent = this.filePath.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        if (Files.notExists(this.filePath)) {
            Files.createFile(this.filePath);
        }
    }

    private String loadContentFromFile() throws IOException {
        String fileContent = Files.readString(this.filePath, StandardCharsets.UTF_8);
        return normalizeLineEndings(fileContent);
    }

    private void saveContentToFile() throws IOException {
        Files.writeString(
            this.filePath,
            this.content,
            StandardCharsets.UTF_8,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE
        );
    }

    private String normalizeLineEndings(String value) {
        return value.replace("\r\n", INTERNAL_NEWLINE).replace("\r", INTERNAL_NEWLINE);
    }

    public String getFilePath() {
        return this.filePath.toString();
    }

    public void appendContent(String content) throws IOException {
        String lineToAppend = content == null ? "" : content;
        if (this.content.isEmpty()) {
            this.content = lineToAppend;
        } else {
            this.content += INTERNAL_NEWLINE + lineToAppend;
        }

        this.cursorPosition = this.content.length();
        saveContentToFile();
    }

    public void overwriteContent(String content) throws IOException {
        this.content = normalizeLineEndings(content == null ? "" : content);
        this.cursorPosition = this.content.length();
        saveContentToFile();
    }

    public String getContent() {
        return this.content;
    }

    public void setSelectionStartPosition(int position) {
        int contentLength = this.content.length();
        if (position < 0) {
            throw new IndexOutOfBoundsException("Selection start preceded file content");
        }
        else if (position > contentLength) {
            throw new IndexOutOfBoundsException("Selection start exceeded file content");
        }
        selectionStartPosition = position;

        if (selectionEndPosition < selectionStartPosition) {
            selectionEndPosition = selectionStartPosition;
        }
    }

    public void setSelectionEndPosition(int position) {
        int contentLength = this.content.length();
        if (position < 0) {
            throw new IndexOutOfBoundsException("Selection end preceded file content");
        }
        else if (position > contentLength) {
            throw new IndexOutOfBoundsException("Selection end exceeded file content");
        }
        else if (position < selectionStartPosition) {
            throw new IndexOutOfBoundsException("Selection end preceded selection start");
        }
        selectionEndPosition = position;
    }

    public EditorState createState() {
        return new EditorState(this.content);
    }

    public void restoreState(EditorState state) throws IOException {
        overwriteContent(state.getContent());
    }

    public int findFirst(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        return this.content.indexOf(searchTerm);
    }

    public List<Integer> findAll(String searchTerm) {
        var indices = new java.util.ArrayList<Integer>();
        if (searchTerm == null || searchTerm.isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        int index = 0;
        while ((index = this.content.indexOf(searchTerm, index)) != -1) {
            indices.add(index);
            index += searchTerm.length();
        }
        return indices;
    }

    public String replaceFirst(String searchTerm, String replacement) throws IOException {
        if (searchTerm == null || searchTerm.isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        int index = this.content.indexOf(searchTerm);
        if (index == -1) {
            return this.content;
        }
        String newContent = this.content.substring(0, index) + (replacement != null ? replacement : "") 
                          + this.content.substring(index + searchTerm.length());
        overwriteContent(newContent);
        return newContent;
    }

    public String replaceAll(String searchTerm, String replacement) throws IOException {
        if (searchTerm == null || searchTerm.isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        String newContent = this.content.replace(searchTerm, replacement != null ? replacement : "");
        overwriteContent(newContent);
        return newContent;
    }
}
