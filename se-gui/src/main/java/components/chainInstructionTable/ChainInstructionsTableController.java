package components.chainInstructionTable;

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


    @FXML
    protected void initialize() {
        // Configure columns once when FXML loads
        colIndex.setCellValueFactory(new PropertyValueFactory<>("instructionNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("instructionTypeStr"));
        colLabel.setCellValueFactory(new PropertyValueFactory<>("labelStr"));
        colInstruction.setCellValueFactory(new PropertyValueFactory<>("command"));
        colCycles.setCellValueFactory(new PropertyValueFactory<>("cyclesNumber"));


    }

    public void fillTable(List<InstructionDTO> instructionChain) {
        if (instructionChain == null || instructionChain.size() <= 1) {
            instructionsTable.getItems().clear();
            return;
        }

        // Replace table content starting from index 1 to the end
        instructionsTable.getItems().setAll(instructionChain.subList(1, instructionChain.size()));
    }

    public void clearChainTable() {
        // Clear the table content
        instructionsTable.getItems().clear();
    }
}
