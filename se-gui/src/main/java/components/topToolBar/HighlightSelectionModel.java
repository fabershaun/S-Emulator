package components.topToolBar;

import dto.ProgramDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;

public class HighlightSelectionModel {
    public static String EMPTY_CHOICE = "- None -";
    private final ObjectProperty<ProgramDTO> currentProgram = new SimpleObjectProperty<>();
    private final ObservableList<String> highlightSelectionOptions = observableArrayList();
    private final StringProperty selectedHighlight = new SimpleStringProperty(null);  // Selected item to highlight, null means no highlight

    public HighlightSelectionModel() {
        currentProgram.addListener(
                (observableValue, oldProgram, newProgram) -> handleProgramChanged(newProgram)
        );
    }

    private void handleProgramChanged(ProgramDTO program) {
        if (program == null) {
            highlightSelectionOptions.clear();
        }

        recalcOptions();
    }

    private void recalcOptions() {
        if (currentProgram.get() == null) {
            highlightSelectionOptions.clear();
            selectedHighlight.set(null);
            return;
        }

        List<String> result = new ArrayList<>();
        result.add(EMPTY_CHOICE);

        if (currentProgram.get().getResult() != null) {
            result.add(currentProgram.get().getResult());
        }
        if (currentProgram.get().getInputVariables() != null) {
            result.addAll(currentProgram.get().getInputVariables());
        }
        if (currentProgram.get().getWorkVariables() != null) {
            result.addAll(currentProgram.get().getWorkVariables());
        }
        if (currentProgram.get().getLabelsStr() != null) {
            result.addAll(currentProgram.get().getLabelsStr());
        }

        highlightSelectionOptions.setAll(result);
        selectedHighlight.set(null);
    }

    // External connection point
    public void setProgram(ProgramDTO programDTO) { currentProgram.set(programDTO); }

    public ObservableList<String> getHighlightOptions() { return FXCollections.unmodifiableObservableList(highlightSelectionOptions); }

    public StringProperty selectedHighlightProperty() { return selectedHighlight; }

    // Set a new highlight value. Passing EMPTY_CHOICE or null clears the selection.
    public void selectHighlight(String value) {
        selectedHighlight.set((value == null || EMPTY_CHOICE.equals(value)) ? null : value);
    }

    public void clearSelection() {
        selectedHighlight.set(EMPTY_CHOICE);
    }
}
