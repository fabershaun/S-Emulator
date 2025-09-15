package components.history;

import components.mainApp.MainAppController;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.Map;

public class HistoryController {

    private MainAppController mainController;
    private ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty;
    private StringProperty programOrFunctionProperty;

    @FXML private VBox historyVBox;
    @FXML private TableView<?> historyTable;
    @FXML private TableColumn<?, ?> colCycles;
    @FXML private TableColumn<?, ?> colDegree;
    @FXML private TableColumn<?, ?> colResult;
    @FXML private TableColumn<?, ?> colRunNumber;
    @FXML private Button reRunButton;
    @FXML private Button showStatusButton;

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setProperty(ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty, StringProperty programOrFunctionProperty) {
        this.programAfterExecuteProperty = programAfterExecuteProperty;
        this.programOrFunctionProperty = programOrFunctionProperty;
    }

    public void initializeListeners() {
        // After new run -> update history table
        programAfterExecuteProperty.addListener((obs, oldProgEx, newProgramExecutorDTO) -> {
            if (newProgramExecutorDTO != null) {
                Map<String, Long> variablesMap = newProgramExecutorDTO.getVariablesToValuesSorted();

            } else {
                historyTable.getItems().clear();
            }
        });

        // TODO: to add if needed
//        programOrFunctionProperty.addListener((obs, oldProg, newProgram) -> {
//
//        })
    }

}
