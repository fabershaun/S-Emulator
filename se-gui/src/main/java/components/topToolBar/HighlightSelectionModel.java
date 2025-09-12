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

    // TODO: Consider to change - made by task in the main controller
    private void recalcOptions() {
        List<String> result = new ArrayList<>();
        result.add(EMPTY_CHOICE);
        result.addAll(currentProgram.get().getLabelsStr());
        result.addAll(currentProgram.get().getInputVariables());

        highlightSelectionOptions.setAll(result);
        selectedHighlight.set(null);
    }

    // External connection point
    public void setProgram(ProgramDTO p) { currentProgram.set(p); }

    public ObservableList<String> getHighlightOptions() { return FXCollections.unmodifiableObservableList(highlightSelectionOptions); }

    public StringProperty selectedHighlightProperty() { return selectedHighlight; }

    // Set a new highlight value. Passing EMPTY_CHOICE or null clears the selection.
    public void selectHighlight(String value) {
        selectedHighlight.set((value == null || EMPTY_CHOICE.equals(value)) ? null : value);
    }
}
