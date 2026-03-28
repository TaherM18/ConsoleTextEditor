package memento;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class EditorRedoHistory {
    private final Deque<EditorState> editorStates;

    public EditorRedoHistory() {
        editorStates = new ArrayDeque<EditorState>();
    }

    public void push(EditorState state) {
        editorStates.push(Objects.requireNonNull(state, "state cannot be null"));
    }

    public EditorState pop() {
        return editorStates.pop();
    }

    public boolean isEmpty() {
        return editorStates.isEmpty();
    }

    public void clear() {
        editorStates.clear();
    }
}
