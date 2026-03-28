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

        while (selectedOption != 6) {
            System.out.println("\n---- Menu ----");
            System.out.println("1. Read content");
            System.out.println("2. Add content");
            System.out.println("3. Erase all content");
            System.out.println("4. Undo");
            System.out.println("5. Redo");
            System.out.println("6. Exit");
            System.out.print("Select an option: ");
            try {
                selectedOption = Integer.parseInt(scanner.nextLine());
                
                switch (selectedOption) {
                    case 1: // Read Content
                        String currentContent = textEditor.getContent();
                        if (currentContent.isEmpty()) {
                            System.out.println("(empty)");
                        } else {
                            System.out.println(currentContent);
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
                    case 6: // Exit
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
