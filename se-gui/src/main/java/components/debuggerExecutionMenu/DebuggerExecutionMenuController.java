package components.debuggerExecutionMenu;

import components.mainApp.MainAppController;
import dto.DebugDTO;
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

    private DebugDTO currentDebugStep;
    private MainAppController mainController;
    private ApplicationMode currentMode = ApplicationMode.NEW_RUN_PRESSED;
    private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;
    private ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty;

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
    @FXML private TableView<VariableRow> inputsTable;
    @FXML private TableColumn<VariableRow, String> colInputs;
    @FXML private TableColumn<VariableRow, Long> colInputsValue;
    @FXML private TableView<Map.Entry<String, Long>> variablesTable;
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
                inputsTable.getItems().clear();
                enterNoProgramLoaded();
            }
        });

        programAfterExecuteProperty.addListener((obs, oldProgEx, newProgramExecutorDTO) -> {
            if (newProgramExecutorDTO != null) {
                Map<String, Long> variablesMap = newProgramExecutorDTO.getVariablesToValuesSorted();
                variablesTable.getItems().setAll(variablesMap.entrySet());
                cyclesNumberLabel.setText(String.valueOf(newProgramExecutorDTO.getTotalCycles()));
            } else {
                variablesTable.getItems().clear();
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
        setDebugControlsDisabled(false);
        inputsTable.setEditable(false);
    }

    public void enterProgramReady() {
        currentMode = ApplicationMode.PROGRAM_READY;
        setNewRunEnabled(true);
        setPlayEnabled(false);
        setModeSelectionEnabled(true);
        setDebugControlsDisabled(false);
        inputsTable.setEditable(false);
    }

    private void enterNewRunPressed() {
        currentMode = ApplicationMode.NEW_RUN_PRESSED;
        setNewRunEnabled(true);
        setModeSelectionEnabled(true);
        setPlayEnabled(true);
        setDebugControlsDisabled(false);
        inputsTable.setEditable(true);
        variablesTable.getItems().clear();
        cyclesNumberLabel.setText(String.valueOf(0));
        resetInputTable(currentSelectedProgramProperty.getValue());
        mainController.clearHistorySelection();
        mainController.disableHistoryAndToolBarComponents(false);
    }

    private void resetInputTable(ProgramDTO program) {
        if (program == null) {
            inputsTable.getItems().clear();
            return;
        }

        List<VariableRow> rows = program.getInputVariables().stream()
                .map(varName -> new VariableRow(varName, 0L))
                .toList();
        inputsTable.getItems().setAll(rows);
    }

    public void prepareForNewRun(List<Long> inputs) {
        enterNewRunPressed(); // Like newRun was pressed

        List<VariableRow> inputTableRows = inputsTable.getItems();      // Update inputs
        for (int i = 0; i < inputTableRows.size() && i < inputs.size(); i++) {
            inputTableRows.get(i).setVariableValue(inputs.get(i));
        }
    }

    private void enterRunning() {
        currentMode = ApplicationMode.RUN;
        setNewRunEnabled(true);
        setModeSelectionEnabled(false);
        setPlayEnabled(false);
        setDebugControlsDisabled(false);
        inputsTable.setEditable(false);
    }

    private void enterDebugging() {
        currentMode = ApplicationMode.DEBUG;
        setNewRunEnabled(true);
        setModeSelectionEnabled(false);
        setPlayEnabled(false);
        setDebugControlsDisabled(true);
        stepBackButton.setDisable(true); // Specific to shot down
        stopButton.setDisable(true);     // Specific to shot down
        inputsTable.setEditable(false);
        mainController.EnterDebugMode();
    }

    private void setPlayEnabled(boolean enabled) {
        playButton.setDisable(!enabled);
    }

    private void setDebugControlsDisabled(boolean disable) {
        stopButton.setDisable(!disable);
        resumeButton.setDisable(!disable);
        stepBackButton.setDisable(!disable);
        stepOverButton.setDisable(!disable);
    }

    private void setNewRunEnabled(boolean enabled) {
        newRunButton.setDisable(!enabled);
    }

    private void setModeSelectionEnabled(boolean enabled) {
        runRadio.setDisable(!enabled);
        debugRadio.setDisable(!enabled);
    }

    @FXML
    private void onNewRun() {
        enterNewRunPressed();
    }

    @FXML
    private void onPlay() {
        List<Long> inputValues = inputsTable.getItems()
                .stream()
                .map(VariableRow::getVariableValue) // take the user input values
                .toList();

        if (runRadio.isSelected()) {
            enterRunning();
            mainController.runProgram(inputValues);
        } else if (debugRadio.isSelected()) {
            mainController.initializeDebugger(inputValues); // Important
            enterDebugging();
        }
    }

    @FXML
    private void onStop() {
        mainController.debugStop();
        updateControllerAfterStep(currentDebugStep);
        stopDebug();
    }

    @FXML
    private void onResume() {
        currentDebugStep = mainController.debugResume();
        updateControllerAfterStep(currentDebugStep);

        if (!currentDebugStep.hasMoreInstructions()) {    // Finish only when there are no more instructions
            stopDebug();
        }
    }

    private void stopDebug() {
        setDebugControlsDisabled(false);
        mainController.finishDebug(currentDebugStep);
    }

    @FXML
    private void onStepOver() {
        currentDebugStep = mainController.debugStepOver();
        updateControllerAfterStep(currentDebugStep);

        if (!currentDebugStep.hasMoreInstructions()) { // When reached the last instruction, shout down all debug buttons
            stopDebug();
        } else {
            stepBackButton.setDisable(false);
            stopButton.setDisable(false);
        }
    }

    @FXML
    private void onStepBack() {
        currentDebugStep = mainController.debugStepBack();
        updateControllerAfterStep(currentDebugStep);

        if (currentDebugStep.getCurrentInstructionNumber() == 0) { // When reached the first instruction, shout down step back button
            stepBackButton.setDisable(true);
        }
    }

    private void updateControllerAfterStep(DebugDTO debugStep) {
        variablesTable.getItems().setAll(debugStep.getDebugProgramExecutorDTO().getVariablesToValuesSorted().entrySet());
        cyclesNumberLabel.setText(String.valueOf(debugStep.getDebugProgramExecutorDTO().getTotalCycles()));
        highlightTargetVariable(debugStep.getTargetVariable());
    }

    private void highlightTargetVariable(String variableName) {
        if (variableName == null) {
            return;
        }

        for (int i = 0; i < variablesTable.getItems().size(); i++) {
            Map.Entry<String, Long> entry = variablesTable.getItems().get(i);
            if (entry.getKey().equals(variableName)) {
                variablesTable.getSelectionModel().select(i);
                variablesTable.scrollTo(i);
                break;
            }
        }
    }
}

