package components.execution.mainInstructionsTable;

import dto.v2.InstructionDTO;
import dto.v2.InstructionsDTO;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MainInstructionsTableController {

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colInstruction;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;
    @FXML private TableColumn<InstructionDTO, Boolean> colBreakPoint;

    public void fillTable(InstructionsDTO instructions) {
    }
}
