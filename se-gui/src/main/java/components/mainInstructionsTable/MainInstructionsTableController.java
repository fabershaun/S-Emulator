package components.mainInstructionsTable;

import components.mainApp.MainAppController;
import dto.InstructionDTO;
import dto.ProgramDTO;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for the main instructions table.
 * Handles program updates and row selection behavior.
 */
public class MainInstructionsTableController {

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colInstruction;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;

    private MainAppController mainController;
    private ObjectProperty<ProgramDTO> currentProgramProperty;

    @FXML
    protected void initialize() {
        colIndex.setCellValueFactory(new PropertyValueFactory<>("instructionNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("instructionTypeStr"));
        colLabel.setCellValueFactory(new PropertyValueFactory<>("labelStr"));
        colInstruction.setCellValueFactory(new PropertyValueFactory<>("command"));
        colCycles.setCellValueFactory(new PropertyValueFactory<>("cyclesNumber"));
    }

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setProperty(ObjectProperty<ProgramDTO> programProperty) {
        this.currentProgramProperty = programProperty;
    }

    public void initializeListeners() {
        // Listen to program changes and repopulate the table
        currentProgramProperty.addListener((obs, oldProg, newProgram) -> {
            if (newProgram != null) {
                instructionsTable.getItems().setAll(
                        newProgram.getInstructions().getProgramInstructionsDtoList()
                );
            } else {
                instructionsTable.getItems().clear();
            }
        });

        // Listen to row selection and notify the main controller
        instructionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem == null) {
                if (mainController != null) {
                    mainController.onInstructionDeselected();
                }
            } else {
                if (mainController != null) {
                    mainController.onInstructionSelected(newItem);
                }
            }
        });
    }
}
