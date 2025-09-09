package components.debuggerExecutionMenu;

import components.mainApp.MainAppController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class DebuggerExecutionMenuController {

    private MainAppController mainController;

    @FXML private Button runButton;
    @FXML private Button debugButton;
    @FXML private Button resumeButton;
    @FXML private Button stepOverButton;
    @FXML private Button stopButton;
    @FXML private TextField cyclesTextField;
    @FXML private TableView<?> variablesTableView;
    @FXML private TableView<?> inputsTableView;

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {

    }
}

