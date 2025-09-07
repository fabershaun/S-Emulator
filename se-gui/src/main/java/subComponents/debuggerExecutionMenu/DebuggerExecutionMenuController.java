package subComponents.debuggerExecutionMenu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import subComponents.fullApp.FullAppController;

public class DebuggerExecutionMenuController {

    private FullAppController mainController;
    @FXML private Button runButton;
    @FXML private Button debugButton;
    @FXML private Button resumeButton;
    @FXML private Button stepOverButton;
    @FXML private Button stopButton;
    @FXML private TextField cyclesTextField;
    @FXML private TableView<?> variablesTableView;
    @FXML private TableView<?> inputsTableView;

    public void setMainController(FullAppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {

    }
}

