package components.topToolBar;

import dto.ProgramDTO;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;

public class ExpansionCollapseModel {
    private final ObjectProperty<ProgramDTO> currentProgram = new SimpleObjectProperty<>();
    private final IntegerProperty currentDegree = new SimpleIntegerProperty(0);
    private final IntegerProperty maxDegree = new SimpleIntegerProperty(0);

    private final ObservableList<Integer> collapseOptions = observableArrayList();
    private final ObservableList<Integer> expandOptions = observableArrayList();


    public ExpansionCollapseModel() {  // Automatic update in any change
        InvalidationListener recalculateDegree = observable -> recalcOptions();
        currentDegree.addListener(recalculateDegree);
        maxDegree.addListener(recalculateDegree);
        currentProgram.addListener(
                (observableValue, oldProgram, newProgram) -> handleProgramChanged(newProgram)
        );
    }

    private void handleProgramChanged(ProgramDTO program) {
        if (program == null) {
            currentDegree.set(0);
            maxDegree.set(0);
        }

        recalcOptions();
    }

    private void recalcOptions() {
        int cur = currentDegree.get();
        int max = maxDegree.get();

        collapseOptions.setAll(range(0, Math.max(cur - 1, -1)));   // 0..cur-1
        expandOptions.setAll(range(cur + 1, max));                 // cur+1..max
    }

    private static List<Integer> range(int start, int end) {
        if (end < start) {
            return List.of();
        }

        List<Integer> rangeListOfOptions = new ArrayList<>(end - start + 1);

        for (int i = start; i <= end; i++) {
            rangeListOfOptions.add(i);
        }

        return rangeListOfOptions;
    }

    // Read-only exposure
    public ReadOnlyIntegerProperty currentDegreeProperty() { return currentDegree; }

    // External connection point
    public void setProgram(ProgramDTO p) { currentProgram.set(p); }
    public void setMaxDegree(int max) { maxDegree.set(max); }
    public void setCurrentDegree(int cur) { currentDegree.set(cur); }

    public ObservableList<Integer> getCollapseOptions() { return FXCollections.unmodifiableObservableList(collapseOptions); }
    public ObservableList<Integer> getExpandOptions()   { return FXCollections.unmodifiableObservableList(expandOptions); }
}