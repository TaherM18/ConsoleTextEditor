package editor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

    // Feature: Word Count
    public int getWordCount() {
        if (this.content.isEmpty()) {
            return 0;
        }
        return this.content.trim().split("\\s+").length;
    }

    public int getCharacterCount() {
        return this.content.length();
    }

    public int getLineCount() {
        if (this.content.isEmpty()) {
            return 0;
        }
        return this.content.split("\n").length;
    }

    // Feature: Convert Case
    public void convertToUpperCase() throws IOException {
        overwriteContent(this.content.toUpperCase());
    }

    public void convertToLowerCase() throws IOException {
        overwriteContent(this.content.toLowerCase());
    }

    public void convertToTitleCase() throws IOException {
        String[] words = this.content.split("\\s+");
        StringBuilder titleCase = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                titleCase.append(words[i].substring(0, 1).toUpperCase())
                        .append(words[i].substring(1).toLowerCase());
                if (i < words.length - 1) {
                    titleCase.append(" ");
                }
            }
        }
        overwriteContent(titleCase.toString());
    }

    // Feature: Delete Line
    public void deleteLine(int lineNumber) throws IOException {
        if (lineNumber <= 0) {
            throw new IllegalArgumentException("Line number must be greater than 0");
        }
        String[] lines = this.content.split("\n", -1);
        if (lineNumber > lines.length) {
            throw new IndexOutOfBoundsException("Line " + lineNumber + " does not exist");
        }
        
        StringBuilder newContent = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i != lineNumber - 1) {
                if (newContent.length() > 0) {
                    newContent.append("\n");
                }
                newContent.append(lines[i]);
            }
        }
        overwriteContent(newContent.toString());
    }

    // Feature: Insert Line
    public void insertLine(int lineNumber, String lineContent) throws IOException {
        if (lineNumber <= 0) {
            throw new IllegalArgumentException("Line number must be greater than 0");
        }
        String[] lines = this.content.isEmpty() ? new String[]{} : this.content.split("\n", -1);
        if (lineNumber > lines.length + 1) {
            throw new IndexOutOfBoundsException("Cannot insert at line " + lineNumber);
        }
        
        StringBuilder newContent = new StringBuilder();
        for (int i = 0; i <= lines.length; i++) {
            if (i == lineNumber - 1) {
                newContent.append(lineContent);
            }
            if (i < lines.length) {
                if (i == lineNumber - 1) {
                    newContent.append("\n");
                }
                newContent.append(lines[i]);
                if (i < lines.length - 1) {
                    newContent.append("\n");
                }
            }
        }
        overwriteContent(newContent.toString());
    }

    // Feature: Trim Whitespace
    public void trimWhitespace() throws IOException {
        String[] lines = this.content.split("\n");
        StringBuilder trimmed = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            trimmed.append(lines[i].trim());
            if (i < lines.length - 1) {
                trimmed.append("\n");
            }
        }
        overwriteContent(trimmed.toString());
    }

    // Feature: Regex Search
    public List<Integer> findByRegex(String pattern) {
        var indices = new java.util.ArrayList<Integer>();
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty");
        }
        try {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(this.content);
            while (m.find()) {
                indices.add(m.start());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid regex pattern: " + e.getMessage());
        }
        return indices;
    }

    // Feature: Next/Previous Match Navigation
    private String lastSearchTerm = null;
    private int currentMatchIndex = -1;
    private List<Integer> lastSearchResults = null;

    public int getNextMatch(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        
        if (!searchTerm.equals(lastSearchTerm)) {
            lastSearchTerm = searchTerm;
            lastSearchResults = findAll(searchTerm);
            currentMatchIndex = -1;
        }
        
        if (lastSearchResults.isEmpty()) {
            return -1;
        }
        
        currentMatchIndex++;
        if (currentMatchIndex >= lastSearchResults.size()) {
            currentMatchIndex = 0;
        }
        
        return lastSearchResults.get(currentMatchIndex);
    }

    public int getPreviousMatch(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            throw new IllegalArgumentException("Search term cannot be null or empty");
        }
        
        if (!searchTerm.equals(lastSearchTerm)) {
            lastSearchTerm = searchTerm;
            lastSearchResults = findAll(searchTerm);
            currentMatchIndex = lastSearchResults.size();
        }
        
        if (lastSearchResults.isEmpty()) {
            return -1;
        }
        
        currentMatchIndex--;
        if (currentMatchIndex < 0) {
            currentMatchIndex = lastSearchResults.size() - 1;
        }
        
        return lastSearchResults.get(currentMatchIndex);
    }
}
