package components.topToolBar;

import components.mainApp.MainAppController;
import dto.ProgramDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import static javafx.collections.FXCollections.observableArrayList;

public class ProgramSelectorModel {

    private MainAppController mainController;
    private final ObjectProperty<ProgramDTO> currentProgram = new SimpleObjectProperty<>();
    private final ObservableList<String> programsAndFunctionsNameOptions = observableArrayList();
    private final ObjectProperty<String> selectedProgramOrFunctionName = new SimpleObjectProperty<>();

    public ProgramSelectorModel() {  // Automatic update in any change
        currentProgram.addListener(
                (observableValue, oldProgram, newProgram) -> handleProgramChanged(newProgram)
        );
    }

    private void handleProgramChanged(ProgramDTO program) {
        if (program == null) {
            programsAndFunctionsNameOptions.clear();
        }

        recalcOptions();
    }

    private void recalcOptions() {
        List<String> programsUserString = mainController.getAllPrograms()
                .stream()
                .map(ProgramDTO::getProgramUserString) // extract programName from each ProgramDTO
                .toList();

        programsAndFunctionsNameOptions.setAll(programsUserString);
        selectedProgramOrFunctionName.set(null);
    }

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setProgram(ProgramDTO programDTO) { currentProgram.set(programDTO); }

    public ObservableList<String> getProgramAndFunctionsOptions() {
        return FXCollections.unmodifiableObservableList(programsAndFunctionsNameOptions);
    }

    public ObjectProperty<String> selectedProgramProperty() { return selectedProgramOrFunctionName; }

    public void selectProgramOrFunction(String programName) {
        selectedProgramOrFunctionName.set(programName);
    }

    public ObservableObjectValue<ProgramDTO> currentProgramProperty() {
        return currentProgram;
    }
}
