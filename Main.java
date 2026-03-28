import editor.EditorHistory;
import editor.EditorRedoHistory;
import editor.TextEditor;

import java.io.IOException;
import java.util.Scanner;
import java.util.InputMismatchException;

public class Main {
    public static void main(String[] args) {
        String editorFilePath = args.length > 0 ? args[0] : "./editor-content.txt";
        TextEditor textEditor = null;
        
        try {
            textEditor = new TextEditor(editorFilePath);
        } catch (IOException e) {
            System.out.println("Failed to initialize editor file: " + e.getMessage());
            return;
        }

        var scanner = new Scanner(System.in);
        var editorHistory = new EditorHistory();

        int selectedOption = 0;
        var editorRedoHistory = new EditorRedoHistory();

        System.out.println("Your TextEditor is initialized");
        System.out.println("File: " + textEditor.getFilePath());

        while (selectedOption != 14) {
            System.out.println("\n---- Menu ----");
            System.out.println("1. Read content");
            System.out.println("2. Add content");
            System.out.println("3. Erase all content");
            System.out.println("4. Undo");
            System.out.println("5. Redo");
            System.out.println("6. Find");
            System.out.println("7. Find and Replace");
            System.out.println("8. Word Count & Statistics");
            System.out.println("9. Convert Case");
            System.out.println("10. Delete Line");
            System.out.println("11. Insert Line");
            System.out.println("12. Trim Whitespace");
            System.out.println("13. Regex Search");
            System.out.println("14. Exit");
            System.out.print("Select an option: ");
            try {
                selectedOption = Integer.parseInt(scanner.nextLine());
                
                switch (selectedOption) {
                    case 1: // Read Content
                        String currentContent = textEditor.getContent();
                        if (currentContent.isEmpty()) {
                            System.out.println("(empty)");
                        } else {
                            String[] lines = currentContent.split("\n");
                            for (int i = 0; i < lines.length; i++) {
                                System.out.println((i + 1) + ": " + lines[i]);
                            }
                        }
                        break;
                    case 2: // Add content
                        System.out.println("Enter your content:");
                        String userContent = scanner.nextLine();

                        editorHistory.push(textEditor.createState());
                        textEditor.appendContent(userContent);
                        editorRedoHistory.clear();
                        System.out.println("Content added to editor");
                        break;
                    case 3: // Erase All Content
                        editorHistory.push(textEditor.createState());
                        textEditor.overwriteContent("");
                        System.out.println("All content erased");
                        break;
                    case 4: // Undo
                        if (editorHistory.isEmpty()) {
                            System.out.println("No history to undo");
                            break;
                        }
                        editorRedoHistory.push(textEditor.createState());
                        textEditor.restoreState(editorHistory.pop());
                        System.out.println("Undid last change");
                        break;
                    case 5: // Redo
                        if (editorRedoHistory.isEmpty()) {
                            System.out.println("No history to redo");
                            break;
                        }
                        editorHistory.push(textEditor.createState());
                        textEditor.restoreState(editorRedoHistory.pop());
                        System.out.println("Redid last undone change");
                        break;
                    case 6: // Find
                        System.out.println("Enter text to find:");
                        String searchTerm = scanner.nextLine();
                        var occurrences = textEditor.findAll(searchTerm);
                        if (occurrences.isEmpty()) {
                            System.out.println("No matches found for: " + searchTerm);
                        } else {
                            System.out.println("Found " + occurrences.size() + " match(es) at position(s): " + occurrences);
                        }
                        break;
                    case 7: // Find and Replace
                        System.out.println("Enter text to find:");
                        String findTerm = scanner.nextLine();
                        System.out.println("Enter replacement text:");
                        String replacementText = scanner.nextLine();
                        
                        var matchesFound = textEditor.findAll(findTerm);
                        if (matchesFound.isEmpty()) {
                            System.out.println("No matches found for: " + findTerm);
                            break;
                        }
                        
                        System.out.println("Found " + matchesFound.size() + " match(es). Replace all? (yes/no):");
                        String confirmReplace = scanner.nextLine();
                        
                        if (confirmReplace.equalsIgnoreCase("yes") || confirmReplace.equalsIgnoreCase("y")) {
                            editorHistory.push(textEditor.createState());
                            textEditor.replaceAll(findTerm, replacementText);
                            editorRedoHistory.clear();
                            System.out.println("Replaced " + matchesFound.size() + " occurrence(s)");
                        } else {
                            System.out.println("Replace cancelled");
                        }
                        break;
                    case 8: // Word Count & Statistics
                        System.out.println("\n---- Statistics ----");
                        System.out.println("Words: " + textEditor.getWordCount());
                        System.out.println("Characters: " + textEditor.getCharacterCount());
                        System.out.println("Lines: " + textEditor.getLineCount());
                        break;
                    case 9: // Convert Case
                        System.out.println("Choose conversion:");
                        System.out.println("1. Convert to UPPERCASE");
                        System.out.println("2. Convert to lowercase");
                        System.out.println("3. Convert to Title Case");
                        System.out.print("Select option: ");
                        try {
                            int caseOption = Integer.parseInt(scanner.nextLine());
                            editorHistory.push(textEditor.createState());
                            switch (caseOption) {
                                case 1:
                                    textEditor.convertToUpperCase();
                                    System.out.println("Converted to UPPERCASE");
                                    break;
                                case 2:
                                    textEditor.convertToLowerCase();
                                    System.out.println("Converted to lowercase");
                                    break;
                                case 3:
                                    textEditor.convertToTitleCase();
                                    System.out.println("Converted to Title Case");
                                    break;
                                default:
                                    System.out.println("Invalid option");
                                    break;
                            }
                            editorRedoHistory.clear();
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input");
                        }
                        break;
                    case 10: // Delete Line
                        System.out.println("Enter line number to delete:");
                        try {
                            int lineNum = Integer.parseInt(scanner.nextLine());
                            editorHistory.push(textEditor.createState());
                            textEditor.deleteLine(lineNum);
                            editorRedoHistory.clear();
                            System.out.println("Line " + lineNum + " deleted");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid line number");
                        }
                        break;
                    case 11: // Insert Line
                        System.out.println("Enter line number to insert at:");
                        try {
                            int insertLineNum = Integer.parseInt(scanner.nextLine());
                            System.out.println("Enter content for the new line:");
                            String newLineContent = scanner.nextLine();
                            editorHistory.push(textEditor.createState());
                            textEditor.insertLine(insertLineNum, newLineContent);
                            editorRedoHistory.clear();
                            System.out.println("Line inserted at position " + insertLineNum);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid line number");
                        }
                        break;
                    case 12: // Trim Whitespace
                        editorHistory.push(textEditor.createState());
                        textEditor.trimWhitespace();
                        editorRedoHistory.clear();
                        System.out.println("Whitespace trimmed from all lines");
                        break;
                    case 13: // Regex Search
                        System.out.println("Enter regex pattern:");
                        String pattern = scanner.nextLine();
                        try {
                            var regexMatches = textEditor.findByRegex(pattern);
                            if (regexMatches.isEmpty()) {
                                System.out.println("No matches found for pattern: " + pattern);
                            } else {
                                System.out.println("Found " + regexMatches.size() + " match(es) at position(s): " + regexMatches);
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case 14: // Exit
                        System.out.println("Exiting TextEditor...");
                        scanner.close();
                        break;
                    default:
                        System.out.println("Please enter one of the given options");
                        break;
                }
            } 
            catch (InputMismatchException e) {
                scanner.nextLine();
            }
            catch (IOException e) {
                System.out.println("File operation failed: " + e.getMessage());
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Try again.");
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
