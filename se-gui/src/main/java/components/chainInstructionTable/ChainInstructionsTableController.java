package components.chainInstructionTable;

import components.mainApp.MainAppController;
import dto.InstructionDTO;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class ChainInstructionsTableController {

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colInstruction;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;

    private MainAppController mainController;

    @FXML
    protected void initialize() {
        // Configure columns once when FXML loads
        colIndex.setCellValueFactory(new PropertyValueFactory<>("instructionNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("instructionTypeStr"));
        colLabel.setCellValueFactory(new PropertyValueFactory<>("labelStr"));
        colInstruction.setCellValueFactory(new PropertyValueFactory<>("command"));
        colCycles.setCellValueFactory(new PropertyValueFactory<>("cyclesNumber"));
    }

    public void setMainController(MainAppController mainController) {
        // Keep pointer for symmetry or future callbacks
        this.mainController = mainController;
    }

    public void fillTable(List<InstructionDTO> instructionChain) {
        // Replace table content with the given chain
        instructionsTable.getItems().setAll(instructionChain);
    }

    public void clearHistory() {
        // Clear the table content
        instructionsTable.getItems().clear();
    }
}
