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
    private final ObjectProperty<ProgramDTO> mainProgramLoadedProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ProgramDTO> selectedProgramProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<String> selectedUserString = new SimpleObjectProperty<>();
    private final ObservableList<String> programsAndFunctionsNameOptions = observableArrayList();

    public ProgramSelectorModel() {  // Automatic update in any change
        mainProgramLoadedProperty.addListener((observableValue, oldProgram, newMainProgram) -> {
                    if (newMainProgram == null) {
                        programsAndFunctionsNameOptions.clear();
                        selectedUserString.set(null);
                    } else {
                        recalculateOptions();
                    }
                });

        selectedUserString.addListener((obs, oldUserString, newUserString) -> {
            if (mainController != null && newUserString != null) {
                ProgramDTO selectedProgram = mainController.getChosenProgramByUserString(newUserString);
                selectedProgramProperty.set(selectedProgram);
            } else {
                selectedProgramProperty.set(null);
            }
        });
    }

    private void recalculateOptions() {
        List<String> programsUserString = mainController.getAllPrograms()
                .stream()
                .map(ProgramDTO::getProgramUserString) // extract programName from each ProgramDTO
                .toList();

        programsAndFunctionsNameOptions.setAll(programsUserString);
        selectedUserString.set(null);
    }

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setMainProgram(ProgramDTO programDTO) { mainProgramLoadedProperty.set(programDTO); }

    public void setSelectedProgram(ProgramDTO programDTO) { selectedProgramProperty.set(programDTO); }

    public ObservableList<String> getProgramAndFunctionsOptions() {
        return FXCollections.unmodifiableObservableList(programsAndFunctionsNameOptions);
    }

    public ObjectProperty<String> selectedUserStringProperty() { return selectedUserString; }

    public String getSelectedUserString() {
        if (selectedUserString.get() == null) {
            return mainProgramLoadedProperty.get().getProgramUserString();
        }

        return selectedUserString.get();
    }

    public void setSelectedProgram(String programName) {
        selectedUserString.set(programName);
    }


    public ObservableObjectValue<ProgramDTO> getMainProgramLoadedProperty() {
        return mainProgramLoadedProperty;
    }
}

