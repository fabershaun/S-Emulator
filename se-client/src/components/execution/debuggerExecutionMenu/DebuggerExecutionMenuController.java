package components.execution.debuggerExecutionMenu;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Map;

public class DebuggerExecutionMenuController {

    @FXML private VBox debuggerExecutionMenu;
    @FXML private Button newRunButton;
    @FXML private RadioButton runRadio;
    @FXML private RadioButton debugRadio;
    @FXML private ToggleGroup runModeToggleGroup;
    @FXML private Button stopButton;
    @FXML private Button resumeButton;
    @FXML private Button stepBackButton;
    @FXML private Button stepOverButton;
    @FXML private Button playButton;
    @FXML private Label cyclesNumberLabel;
    @FXML private TableView<VariableRowV3> inputsTable;
    @FXML private TableColumn<VariableRowV3, String> colInputs;
    @FXML private TableColumn<VariableRowV3, Long> colInputsValue;
    @FXML private TableView<Map.Entry<String, Long>> variablesTable;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colVariables;
    @FXML private TableColumn<Map.Entry<String, Long>, Long> colVariablesValue;


    @FXML
    void onNewRun() {

    }

    @FXML
    void onPlay() {

    }

    @FXML
    void onResume() {

    }

    @FXML
    void onStepBack() {

    }

    @FXML
    void onStepOver() {

    }

    @FXML void onStop() {

    }
}
