package components.debuggerExecutionMenu;

import components.mainApp.MainAppController;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.LongStringConverter;

import java.util.List;
import java.util.Map;


public class DebuggerExecutionMenuController {

    private MainAppController mainController;
    private ApplicationMode currentMode = ApplicationMode.NEW_RUN_PRESSED;
    private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;
    private ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty;

    @FXML private Button newRunButton;
    @FXML private RadioButton runRadio;
    @FXML private RadioButton debugRadio;
    @FXML private ToggleGroup runModeToggleGroup;
    @FXML private Button resumeButton;
    @FXML private Button stepOverButton;
    @FXML private Button stopButton;
    @FXML private Button playButton;
    @FXML private Label cyclesNumberLabel;
    @FXML private TableView<VariableRow> inputsTableView;
    @FXML private TableColumn<VariableRow, String> colInputs;
    @FXML private TableColumn<VariableRow, Long> colInputsValue;
    @FXML private TableView<Map.Entry<String, Long>> variablesTableView;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colVariables;
    @FXML private TableColumn<Map.Entry<String, Long>, Long> colVariablesValue;

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setProperty(ObjectProperty<ProgramDTO> programProperty, ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty) {
            this.currentSelectedProgramProperty = programProperty;
            this.programAfterExecuteProperty = programAfterExecuteProperty;
    }

    @FXML
    private void initialize() {
        enterNoProgramLoaded();

        // Input table:
        colInputs.setCellValueFactory(new PropertyValueFactory<>("variableName"));
        colInputsValue.setCellValueFactory(new PropertyValueFactory<>("variableValue"));

        configureNumericEditableColumn(colInputsValue);

        // Variable table:
        colVariables.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey()));
        colVariablesValue.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getValue()).asObject());
    }

    public void initializeListeners() {
        currentSelectedProgramProperty.addListener((obs, oldProg, newProgram) -> {
            if (newProgram != null) {
                resetInputTable(newProgram);
                enterProgramReady();
            } else {
                inputsTableView.getItems().clear();
                enterNoProgramLoaded();
            }
        });

        programAfterExecuteProperty.addListener((obs, oldProgEx, newProgramExecutorDTO) -> {
            if (newProgramExecutorDTO != null) {
                Map<String, Long> variablesMap = newProgramExecutorDTO.getVariablesToValuesSorted();
                variablesTableView.getItems().setAll(variablesMap.entrySet());
                cyclesNumberLabel.setText(String.valueOf(newProgramExecutorDTO.getTotalCycles()));
            } else {
                variablesTableView.getItems().clear();
            }
        });
    }

    private void configureNumericEditableColumn(TableColumn<VariableRow, Long> column) {
        column.setEditable(true);

        column.setCellFactory(tc -> {
            TextFieldTableCell<VariableRow, Long> cell =
                    new TextFieldTableCell<>(new LongStringConverter()) {

                        @Override
                        public void startEdit() {
                            super.startEdit();
                            if (getGraphic() instanceof TextField textField) {
                                // Allow only integer input
                                textField.setTextFormatter(new TextFormatter<>(change -> {
                                    String newText = change.getControlNewText();
                                    return newText.matches("-?\\d*") ? change : null;
                                }));

                                // Commit value when focus is lost (not only on Enter)
                                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                                    if (!isNowFocused && isEditing()) {
                                        try {
                                            Long value = Long.parseLong(textField.getText());
                                            commitEdit(value); // Save the new value
                                        } catch (NumberFormatException e) {
                                            cancelEdit(); // Cancel if not a valid integer
                                        }
                                    }
                                });
                            }
                        }
                    };
            return cell;
        });

        // Update the underlying data model when value is committed
        column.setOnEditCommit(event -> {
            VariableRow row = event.getRowValue();
            row.setVariableValue(event.getNewValue());
        });
    }

    private void enterNoProgramLoaded() {
        currentMode = ApplicationMode.NO_PROGRAM_LOADED;
        setNewRunEnabled(false);
        setPlayEnabled(false);
        setModeSelectionEnabled(false);
        setDebugControlsEnabled(false);
        inputsTableView.setEditable(false);
    }

    private void enterProgramReady() {
        currentMode = ApplicationMode.PROGRAM_READY;
        setNewRunEnabled(true);
        setPlayEnabled(false);
        setModeSelectionEnabled(true);
        setDebugControlsEnabled(false);
        inputsTableView.setEditable(false);
    }

    private void enterNewRunPressed() {
        currentMode = ApplicationMode.NEW_RUN_PRESSED;
        setNewRunEnabled(true);
        setModeSelectionEnabled(true);
        setPlayEnabled(true);
        setDebugControlsEnabled(false);
        inputsTableView.setEditable(true);
        variablesTableView.getItems().clear();
        cyclesNumberLabel.setText(String.valueOf(0));
        resetInputTable(currentSelectedProgramProperty.getValue());
    }

    private void resetInputTable(ProgramDTO program) {
        if (program == null) {
            inputsTableView.getItems().clear();
            return;
        }

        List<VariableRow> rows = program.getInputVariables().stream()
                .map(varName -> new VariableRow(varName, 0L))
                .toList();
        inputsTableView.getItems().setAll(rows);
    }

    public void prepareForNewRun(List<Long> inputs) {
        enterNewRunPressed(); // Like newRun was pressed

        List<VariableRow> inputTableRows = inputsTableView.getItems();      // Update inputs
        for (int i = 0; i < inputTableRows.size() && i < inputs.size(); i++) {
            inputTableRows.get(i).setVariableValue(inputs.get(i));
        }
    }

    private void enterRunning() {
        currentMode = ApplicationMode.RUN;
        setNewRunEnabled(true);
        setModeSelectionEnabled(true);
        setPlayEnabled(false);
        setDebugControlsEnabled(false);
        inputsTableView.setEditable(false);
    }

    private void enterDebugging() {
        currentMode = ApplicationMode.DEBUG;
        setNewRunEnabled(true);
        setModeSelectionEnabled(true);
        setPlayEnabled(false);
        setDebugControlsEnabled(true);
        inputsTableView.setEditable(false);
    }


    // Enable/disable Play button
    private void setPlayEnabled(boolean enabled) {
        playButton.setDisable(!enabled);
    }

    // Enable/disable Debug controls (Resume, Stop, StepOver)
    private void setDebugControlsEnabled(boolean enabled) {
        resumeButton.setDisable(!enabled);
        stopButton.setDisable(!enabled);
        stepOverButton.setDisable(!enabled);
    }

    // Enable/disable New Run
    private void setNewRunEnabled(boolean enabled) {
        newRunButton.setDisable(!enabled);
    }

    // Enable/disable Run/Debug radio buttons
    private void setModeSelectionEnabled(boolean enabled) {
        runRadio.setDisable(!enabled);
        debugRadio.setDisable(!enabled);
    }

    @FXML
    private void onNewRun() {
        enterNewRunPressed();
    }

    // todo
    @FXML
    private void onPlay() {
        List<Long> inputValues = inputsTableView.getItems()
                .stream()
                .map(VariableRow::getVariableValue) // take the user input values
                .toList();

        if (runRadio.isSelected()) {
            enterRunning();
            mainController.runProgram(inputValues);
        } else if (debugRadio.isSelected()) {
            enterDebugging();
            // todo: כאן להתחיל Debug
        }
    }

    @FXML
    private void onResume() {
        // פעולה של Resume
    }

    @FXML
    private void onStop() {
        // פעולה של Stop
    }

    @FXML
    private void onStepOver() {
        // פעולה של Step Over
    }
}

