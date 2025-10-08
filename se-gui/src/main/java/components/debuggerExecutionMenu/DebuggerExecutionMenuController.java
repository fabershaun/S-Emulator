package components.debuggerExecutionMenu;

import components.mainApp.MainAppController;
import dto.v2.DebugDTO;
import dto.v2.ProgramDTO;
import dto.v2.ProgramExecutorDTO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.converter.LongStringConverter;

import java.util.List;
import java.util.Map;

public class DebuggerExecutionMenuController {

    private DebugDTO currentDebugStep;
    private MainAppController mainController;
    private ApplicationMode currentMode = ApplicationMode.NEW_RUN_PRESSED;
    private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;
    private ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty;
    private boolean inputsEditableMode = false;     // Flag to control whether blinking is active

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

        // Load custom CSS
        String cssPath = getClass().getResource("/components/debuggerExecutionMenu/debuggerMenu.css").toExternalForm();
        debuggerExecutionMenu.getStylesheets().add(cssPath);

        // Input table:
        colInputs.setCellValueFactory(new PropertyValueFactory<>("variableName"));
        colInputsValue.setCellValueFactory(new PropertyValueFactory<>("variableValue"));

        configureNumericEditableColumn(colInputsValue);

        // Variable table:
        colVariables.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey()));
        colVariablesValue.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getValue()).asObject());

        inputsTable.getSelectionModel().setCellSelectionEnabled(true);
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

                        private final Tooltip tooltip = new Tooltip("Enter a number");

                        // Pulse animation (scale up and down)
                        private final Timeline pulseAnimation = new Timeline(
                                new KeyFrame(Duration.ZERO,
                                        new KeyValue(scaleXProperty(), 1.0),
                                        new KeyValue(scaleYProperty(), 1.0)
                                ),
                                new KeyFrame(Duration.millis(600),
                                        new KeyValue(scaleXProperty(), 1.15),
                                        new KeyValue(scaleYProperty(), 1.15)
                                ),
                                new KeyFrame(Duration.millis(1200),
                                        new KeyValue(scaleXProperty(), 1.0),
                                        new KeyValue(scaleYProperty(), 1.0)
                                )
                        );

                        {
                            // repeat pulse forever
                            pulseAnimation.setCycleCount(Animation.INDEFINITE);
                            setTooltip(tooltip);
                        }

                        @Override
                        public void startEdit() {
                            super.startEdit();
                            if (getGraphic() instanceof TextField textField) {
                                // Allow only integer input
                                textField.setTextFormatter(new TextFormatter<>(change -> {
                                    String newText = change.getControlNewText();
                                    return newText.matches("-?\\d*") ? change : null;
                                }));

                                // Commit value when focus is lost
                                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                                    if (!isNowFocused && isEditing()) {
                                        try {
                                            Long value = Long.parseLong(textField.getText());
                                            commitEdit(value);
                                        } catch (NumberFormatException e) {
                                            cancelEdit();
                                        }
                                    }
                                });

                                // handle TAB
                                textField.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                                    if (event.getCode() == javafx.scene.input.KeyCode.TAB) {
                                        try {
                                            commitEdit(Long.parseLong(textField.getText()));
                                        } catch (NumberFormatException e) {
                                            cancelEdit();
                                        }
                                        event.consume();

                                        TableView.TableViewSelectionModel<VariableRow> selectionModel = getTableView().getSelectionModel();
                                        int currentRow = getIndex();
                                        int nextRow = (currentRow + 1) % getTableView().getItems().size();
                                        selectionModel.clearAndSelect(nextRow, getTableColumn());
                                        getTableView().edit(nextRow, getTableColumn());
                                    }
                                });
                            }
                        }

                        @Override
                        public void updateItem(Long value, boolean empty) {
                            super.updateItem(value, empty);
                            if (empty) {
                                setText(null);
                                pulseAnimation.stop();
                                setScaleX(1.0);
                                setScaleY(1.0);
                                setStyle("-fx-alignment: CENTER;");
                            } else {
                                if (inputsEditableMode && value == 0) {
                                    // Show placeholder instead of 0
                                    setText("Enter value");
                                    setStyle("-fx-text-fill: gray; -fx-font-style: italic; -fx-alignment: CENTER;");

                                    pulseAnimation.play(); // optional pulse effect
                                } else {
                                    // Show normal value
                                    setText(value.toString());
                                    setStyle("-fx-text-fill: black; -fx-alignment: CENTER;");

                                    pulseAnimation.stop();
                                    setScaleX(1.0);
                                    setScaleY(1.0);
                                }
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
        setModeSelectionDisabled(true);
        setDebugControlsDisabled(true);
        inputsTable.setEditable(false);
    }

    public void enterProgramReady() {
        currentMode = ApplicationMode.PROGRAM_READY;
        setNewRunEnabled(true);
        setPlayEnabled(false);
        setModeSelectionDisabled(false);
        setDebugControlsDisabled(true);
        inputsTable.setEditable(false);
    }

    private void enterNewRunPressed() {
        currentMode = ApplicationMode.NEW_RUN_PRESSED;
        setNewRunEnabled(true);
        setModeSelectionDisabled(false);
        setPlayEnabled(true);
        setDebugControlsDisabled(true);
        inputsTable.setEditable(true);
        variablesTable.getItems().clear();
        cyclesNumberLabel.setText(String.valueOf(0));
        resetInputTable(currentSelectedProgramProperty.getValue());
        mainController.clearHistorySelection();
        mainController.disableHistoryAndToolBarComponents(false);
        inputsEditableMode = true; // enable blinking only now
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
        setModeSelectionDisabled(true);
        setPlayEnabled(false);
        setDebugControlsDisabled(true);
        inputsTable.setEditable(false);
    }

    private void enterDebugging() {
        currentMode = ApplicationMode.DEBUG;
        setNewRunEnabled(true);
        setModeSelectionDisabled(true);
        setPlayEnabled(false);
        setDebugControlsDisabled(false);
        stepBackButton.setDisable(true); // Specific to shot down
        stopButton.setDisable(true);     // Specific to shot down
        inputsTable.setEditable(false);
        mainController.EnterDebugMode();
        currentDebugStep = null; // Reset
    }

    private void setPlayEnabled(boolean enabled) {
        playButton.setDisable(!enabled);
    }

    private void setDebugControlsDisabled(boolean disable) {
        stopButton.setDisable(disable);
        resumeButton.setDisable(disable);
        stepBackButton.setDisable(disable);
        stepOverButton.setDisable(disable);
    }

    private void setNewRunEnabled(boolean enabled) {
        newRunButton.setDisable(!enabled);
    }

    private void setModeSelectionDisabled(boolean disabled) {
        runRadio.setDisable(disabled);
        debugRadio.setDisable(disabled);
    }

    @FXML
    private void onNewRun() {
        enterNewRunPressed();
    }

    @FXML
    private void onPlay() {
        inputsEditableMode = false; // disable blinking only now

        List<Long> inputValues = inputsTable.getItems()
                .stream()
                .map(VariableRow::getVariableValue) // take the user input values
                .toList();

        inputsTable.refresh(); // force refresh so placeholders return to 0

        if (runRadio.isSelected()) {
            enterRunning();
            mainController.runProgram(inputValues);
        } else if (debugRadio.isSelected()) {
            mainController.initializeDebugger(inputValues); // Important
            enterDebugging();
            onResume();
        }
    }

    @FXML
    private void onStop() {
        mainController.debugStop();

        // Update the UI only if we already have a valid debug step
        if (currentDebugStep != null) {
            updateControllerAfterStep(currentDebugStep);
        } else {
            mainController.clearVariableTableInDebugController(); // Clear the variable table and rest cycles
        }

        stopDebug();
    }

    @FXML
    private void onResume() {
        mainController.debugResume(debugStep -> {

            currentDebugStep = debugStep;
            updateControllerAfterStep(currentDebugStep);

            if (!currentDebugStep.hasMoreInstructions()) { // When finish
                stopDebug();
            } else {    // When stop at break point -> not finish debug! cant start new run, only debug / stop
                newRunButton.setDisable(true);
                resumeButton.setDisable(false);
                stepOverButton.setDisable(false);
                stepBackButton.setDisable(false);
                stopButton.setDisable(false);
            }
        });

        // While engine is calculating debug resume (long task)
        newRunButton.setDisable(true);
        stopButton.setDisable(false);       // Only 'stop' available while resume
        resumeButton.setDisable(true);
        stepOverButton.setDisable(true);
        stepBackButton.setDisable(true);
    }

    private void stopDebug() {
        setDebugControlsDisabled(true);
        setNewRunEnabled(true);
        setModeSelectionDisabled(false);

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
        variablesTable.getItems().setAll(debugStep.getVariablesToValuesSorted().entrySet());
        cyclesNumberLabel.setText(String.valueOf(debugStep.getTotalCycles()));
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
                scrollToCenter(variablesTable, i);
                break;
            }
        }
    }

    // Scrolls the table so that the given row index will be centered
    public static void scrollToCenter(TableView<?> table, int rowIndex) {
        if (rowIndex < 0 || rowIndex >= table.getItems().size()) {
            return; // invalid index
        }

        double tableHeight = table.getHeight();
        double rowHeight = table.getFixedCellSize() > 0
                ? table.getFixedCellSize()
                : 24; // default row height if not set
        int visibleRows = (int) (tableHeight / rowHeight);

        int targetIndex = Math.max(0, rowIndex - visibleRows / 2);
        table.scrollTo(targetIndex);
    }

    public void clearVariableTableAndResetCycles() {
        variablesTable.getItems().clear();
        cyclesNumberLabel.setText("");
    }
}

