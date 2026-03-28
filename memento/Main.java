package memento;

import java.util.EmptyStackException;
import java.util.Scanner;

import java.io.IOException;
import java.util.InputMismatchException;
public class Main {
    public static void main(String[] args) {
        var textEditor = new TextEditor();
        var editorHistory = new EditorHistory();
        String editorFilePath = args.length > 0 ? args[0] : "memento/editor-content.txt";

        TextEditor textEditor;
        try {
            textEditor = new TextEditor(editorFilePath);
        } catch (IOException e) {
            System.out.println("Failed to initialize editor file: " + e.getMessage());
            return;
        }

        int selectedOption = 0;
        var editorRedoHistory = new EditorRedoHistory();

        System.out.println("Your TextEditor is initialized\n");

        System.out.println("Your TextEditor is initialized");
        System.out.println("File: " + textEditor.getFilePath() + "\n");
            System.out.println("\nMenu:");
        while (selectedOption != 6) {
            System.out.println("2. Add content");
            System.out.println("3. Erase all content");
            System.out.println("4. Undo");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");
            System.out.println("5. Redo");
            System.out.println("6. Exit");
            try {
                selectedOption = Integer.parseInt(scanner.nextLine());
                
                switch (selectedOption) {
                scanner.nextLine();
                    case 1:
                        System.out.println(textEditor.getContent());
                        break;
                        String currentContent = textEditor.getContent();
                        if (currentContent.isEmpty()) {
                            System.out.println("(empty)");
                        } else {
                            System.out.println(currentContent);
                        }
                        System.out.println("Enter your content:");
                        String userContent = scanner.nextLine();
                        System.out.println("Enter your content:");

                        textEditor.appendContent(userContent);
                        editorHistory.push(textEditor.createState());
                        break;
                    case 3:
                        editorRedoHistory.clear();
                        editorHistory.push(textEditor.createState());
                        textEditor.overwriteContent("");
                        System.out.println("All content erased");
                        editorHistory.push(textEditor.createState());
                    case 4:
                        editorRedoHistory.clear();
                        try {
                            var lastEditorState = editorHistory.pop();
                            textEditor.overwriteContent(lastEditorState.getContent());
                        if (editorHistory.isEmpty()) {
                            System.out.println("No history to undo");
                            break;
                        }

                        editorRedoHistory.push(textEditor.createState());
                        var lastEditorState = editorHistory.pop();
                        textEditor.restoreState(lastEditorState);
                            System.out.println("Nothing to undo...");
                        }
                        break;
                        if (editorRedoHistory.isEmpty()) {
                            System.out.println("No history to redo");
                            break;
                        }

                        editorHistory.push(textEditor.createState());
                        var redoEditorState = editorRedoHistory.pop();
                        textEditor.restoreState(redoEditorState);
                        System.out.println("Redid last undone change");
                        break;
                    case 6:
                        System.out.println("Exiting TextEditor...");
                        System.out.println("Exiting TextEditor...");
                        scanner.close();
                        break;
                    default:
                        System.out.println("Please enter one of the given options");
                        break;
            } catch (InputMismatchException e) {
            }
                scanner.nextLine();
            } catch (IOException e) {
                System.out.println("File operation failed: " + e.getMessage());
            catch (NumberFormatException e) {
                System.out.println("Invalid input. Try again.");
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
