package components.instructionsTable;

import components.mainApp.AppState;
import components.mainApp.MainAppController;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

public class InstructionsTableController {

    private MainAppController mainController;
    private AppState state;

    @FXML private TableView<?> table;
    @FXML private TableColumn<?, ?> colCycles;
    @FXML private TableColumn<?, ?> colIndex;
    @FXML private TableColumn<?, ?> colInstruction;
    @FXML private TableColumn<?, ?> colType;

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setState(AppState state) {
        this.state = state;
    }

    @FXML
    private void initialize() {

    }
    @FXML
    void handleRowClick(MouseEvent event) {

    }
}
