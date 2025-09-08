package components.instructionsTable;

import components.mainApp.AppState;
import components.mainApp.MainAppController;
import dto.InstructionDTO;
import dto.ProgramDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

public class InstructionsTableController {

    private MainAppController mainController;
    private AppState state;

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colInstruction;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setState(AppState state) {
        this.state = state;

        this.state.currentProgramProperty().addListener((obs, oldProgram, newProgram) -> {
            if (newProgram != null) {    // ProgramDTO was updated successfully
                fillTable(newProgram);
            }
        });
    }

    @FXML
    private void initialize() {
        // Bind columns to InstructionDTO fields
        colIndex.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getInstructionNumber()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInstructionTypeStr()));
        colLabel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLabelStr()));
        colInstruction.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCommand())); // or getCommand()
        colCycles.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCyclesNumber()));
    }

    private void fillTable(ProgramDTO program) {
        instructionsTable.getItems().setAll(
                program.getInstructions().getProgramInstructionsDtoList()
        );
    }

    @FXML
    void handleRowClick(MouseEvent event) {

    }
}
