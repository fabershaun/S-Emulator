package subComponents.instructionsTable;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import subComponents.fullApp.FullAppController;
import javafx.scene.input.MouseEvent;

public class InstructionsTableController {

    private FullAppController mainController;
    @FXML private TableView<?> table;
    @FXML private TableColumn<?, ?> colCycles;
    @FXML private TableColumn<?, ?> colIndex;
    @FXML private TableColumn<?, ?> colInstruction;
    @FXML private TableColumn<?, ?> colType;

    public void setMainController(FullAppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {

    }
    @FXML
    void handleRowClick(MouseEvent event) {

    }
}
