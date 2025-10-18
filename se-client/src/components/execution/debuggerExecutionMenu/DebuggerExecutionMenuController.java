package components.execution.debuggerExecutionMenu;

import dto.v3.ArchitectureDTO;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import utils.general.GeneralUtils;
import components.execution.mainExecution.MainExecutionController;
import dto.v2.DebugDTO;
import dto.v2.ProgramDTO;
import dto.v2.ProgramExecutorDTO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.converter.LongStringConverter;

import java.util.List;
import java.util.Map;

import static utils.Constants.*;

public class DebuggerExecutionMenuController {

    private DebugDTO currentDebugStep;
    private MainExecutionController executionController;
    private ApplicationMode currentMode = ApplicationMode.NEW_RUN_PRESSED;
    private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;
    private ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty;
    private StringProperty architectureRepresentationProperty;
    private IntegerProperty architectureRankProperty;
    private boolean inputsEditableMode = false;     // Flag to control whether blinking is active

    @FXML private VBox debuggerExecutionMenu;
    @FXML private Button newRunButton;
    @FXML private ToggleGroup runModeToggleGroup;
    @FXML private RadioButton runRadio;
    @FXML private RadioButton debugRadio;
    @FXML private ComboBox<ArchitectureDTO> architectureComboBox;
    @FXML private Button playButton;
    @FXML private Button stopButton;
    @FXML private Button resumeButton;
    @FXML private Button stepBackButton;
    @FXML private Button stepOverButton;
    @FXML private Label cyclesNumberLabel;
    @FXML private TableView<VariableRowV3> inputsTable;
    @FXML private TableColumn<VariableRowV3, String> colInputs;
    @FXML private TableColumn<VariableRowV3, Long> colInputsValue;
    @FXML private TableView<Map.Entry<String, Long>> variablesTable;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colVariables;
    @FXML private TableColumn<Map.Entry<String, Long>, Long> colVariablesValue;


    public void setExecutionController(MainExecutionController executionController) {
        this.executionController = executionController;
    }

    public void setProperty(ObjectProperty<ProgramDTO> programProperty, ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty, StringProperty chosenArchitecture, IntegerProperty architectureRankProperty) {
        this.currentSelectedProgramProperty = programProperty;
        this.programAfterExecuteProperty = programAfterExecuteProperty;
        this.architectureRepresentationProperty = chosenArchitecture;
        this.architectureRankProperty = architectureRankProperty;
    }

    public ComboBox<ArchitectureDTO> getArchitectureComboBox() {
        return architectureComboBox;
    }

    @FXML
    private void initialize() {
        enterNoProgramLoaded();

        // Load custom CSS
        String cssPath = getClass().getResource(DEBUGGER_CSS).toExternalForm();
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

        // Combo box:
        architectureComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ArchitectureDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Architecture");
                } else {
                    setText(item.getRepresentation());
                }
            }
        });

        architectureComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(ArchitectureDTO item) {
                return item == null ? "" : item.getRepresentation();
            }

            @Override
            public ArchitectureDTO fromString(String string) {
                return null;
            }
        });
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

        architectureComboBox.disableProperty().addListener((obs, wasDisabled, isNowDisabled) -> {
            if (isNowDisabled) {
                executionController.clearArchitectureColors();
            }
        });

        architectureComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldArchitecture, newArchitectureDTO) -> {
            if (newArchitectureDTO == null) {
                playButton.setDisable(true);
                return;
            }

            architectureRepresentationProperty.set(newArchitectureDTO.getRepresentation());
            architectureRankProperty.set(newArchitectureDTO.getRank());
        });

        architectureRankProperty.addListener((obs, oldRank, newRank) -> {
            if (newRank == null) return;

            ProgramDTO currentProgram = currentSelectedProgramProperty.get();
            if (currentProgram == null) return;

            int minimumRankProgramRequired = currentProgram.getMiniminRequireRank();
            int selectedArchitectureRank = architectureRankProperty.get();
            playButton.setDisable(minimumRankProgramRequired > selectedArchitectureRank);
        });
    }

    private void loadArchitectureTypes() {
        executionController.loadArchitectureTypes();
    }

    private void configureNumericEditableColumn(TableColumn<VariableRowV3, Long> column) {
        column.setEditable(true);

        column.setCellFactory(tc -> {
            TextFieldTableCell<VariableRowV3, Long> cell =
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

                                        TableView.TableViewSelectionModel<VariableRowV3> selectionModel = getTableView().getSelectionModel();
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
            VariableRowV3 row = event.getRowValue();
            row.setVariableValue(event.getNewValue());
        });
    }

    private void enterNoProgramLoaded() {
        currentMode = ApplicationMode.NO_PROGRAM_LOADED;
        setNewRunEnabled(false);
        setPlayEnabled(false);
        setModeSelectionDisabled(true);
        setDebugControlsDisabled(true);
        setArchitectureComboBoxDisabled(true);
        inputsTable.setEditable(false);
    }

    public void enterProgramReady() {
        currentMode = ApplicationMode.PROGRAM_READY;
        setNewRunEnabled(true);
        setPlayEnabled(false);
        setModeSelectionDisabled(false);
        setDebugControlsDisabled(true);
        setArchitectureComboBoxDisabled(true);
        inputsTable.setEditable(false);
    }

    public void enterNewRunPressed() {
        currentMode = ApplicationMode.NEW_RUN_PRESSED;
        setNewRunEnabled(true);
        setModeSelectionDisabled(false);

        setPlayEnabled(false);  // need to choose architecture first
        setDebugControlsDisabled(true);
        setArchitectureComboBoxDisabled(false);
        inputsTable.setEditable(true);
        variablesTable.getItems().clear();
        cyclesNumberLabel.setText(String.valueOf(0));
        resetInputTable(currentSelectedProgramProperty.getValue());
        executionController.disableToolBarComponents(false);
        inputsEditableMode = true; // enable blinking only now

        architectureRankProperty.set(0); // Important: to reset the last choice
        architectureRepresentationProperty.set("");

        loadArchitectureTypes();
    }

    private void enterRunning() {
        currentMode = ApplicationMode.RUN;
        setNewRunEnabled(true);
        setModeSelectionDisabled(true);
        setPlayEnabled(false);
        setDebugControlsDisabled(true);
        setArchitectureComboBoxDisabled(true);
        inputsTable.setEditable(false);
    }

    public void enterDebugging() {
        currentMode = ApplicationMode.DEBUG;
        setNewRunEnabled(true);
        setModeSelectionDisabled(true);
        setPlayEnabled(false);
        setDebugControlsDisabled(false);
        stepBackButton.setDisable(true); // Specific to shot down
        stopButton.setDisable(true);     // Specific to shot down
        inputsTable.setEditable(false);
        setArchitectureComboBoxDisabled(true);
        executionController.EnterDebugMode();
        currentDebugStep = null; // Reset
    }

    private void resetInputTable(ProgramDTO program) {
        if (program == null) {
            inputsTable.getItems().clear();
            return;
        }

        List<VariableRowV3> rows = program.getInputVariables().stream()
                .map(varName -> new VariableRowV3(varName, 0L))
                .toList();
        inputsTable.getItems().setAll(rows);
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

    private void setArchitectureComboBoxDisabled(boolean disabled) {
        architectureComboBox.setDisable(disabled);
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
        boolean isEnoughCredits = executionController.checkIfHasEnoughCreditsToPlay();
        if (!isEnoughCredits) {
            return;
        }

        inputsEditableMode = false; // disable blinking only now

        List<Long> inputValues = inputsTable.getItems()
                .stream()
                .map(VariableRowV3::getVariableValue) // take the user input values
                .toList();

        inputsTable.refresh(); // force refresh so placeholders return to 0

        if (runRadio.isSelected()) {
            enterRunning();
            executionController.runProgram(inputValues);
        } else if (debugRadio.isSelected()) {
            executionController.initializeDebugger(inputValues); // Important
            enterDebugging();
        }
    }

    @FXML
    private void onStop() {
        executionController.debugStop();

        // Update the UI only if we already have a valid debug step
        if (currentDebugStep != null) {
            updateControllerAfterStep(currentDebugStep);
        } else {
            clearVariableTableAndResetCycles(); // Clear the variable table and rest cycles
        }

        stopDebug();
    }

    @FXML
    public void onResume() {
        executionController.debugResume(debugStep -> {

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

        executionController.finishDebug();
    }

    @FXML
    private void onStepOver() {
        // While server calculating
        disableDebugButtonsDuringServerCall();

        executionController.debugStepOver(debugStep -> {
            currentDebugStep = debugStep;
            updateControllerAfterStep(currentDebugStep);

            if (!currentDebugStep.hasMoreInstructions()) {
                stopDebug();
            } else {
                stepOverButton.setDisable(false);
                stepBackButton.setDisable(false);
                stopButton.setDisable(false);
                resumeButton.setDisable(false);
            }
        });
    }

    @FXML
    private void onStepBack() {
        // While server calculating
        disableDebugButtonsDuringServerCall();

        executionController.debugStepBack( debugStep -> {
            currentDebugStep = debugStep;
            updateControllerAfterStep(currentDebugStep);

            stepOverButton.setDisable(false);
            stopButton.setDisable(false);
            resumeButton.setDisable(false);
            stepBackButton.setDisable(currentDebugStep.getCurrentInstructionNumber() == 0);  // When reached the first instruction, shout down step back button
        });
    }

    private void disableDebugButtonsDuringServerCall() {
        stopButton.setDisable(false);
        stepOverButton.setDisable(true);
        resumeButton.setDisable(true);
        stepBackButton.setDisable(true);
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
                GeneralUtils.scrollToCenter(variablesTable, i);
                break;
            }
        }
    }

    public void clearVariableTableAndResetCycles() {
        variablesTable.getItems().clear();
        cyclesNumberLabel.setText("");
    }

    // When re-run was pressed
    public void prepareForNewRun(List<Long> inputs, String chosenArchitecture) {
        enterNewRunPressed(); // Like newRun was pressed

        // Update inputs
        List<VariableRowV3> inputTableRows = inputsTable.getItems();
        for (int i = 0; i < inputTableRows.size() && i < inputs.size(); i++) {
            inputTableRows.get(i).setVariableValue(inputs.get(i));
        }

        // Select the matching architecture in the ComboBox
        if (chosenArchitecture != null && !chosenArchitecture.isEmpty()) {
            final javafx.collections.ListChangeListener<ArchitectureDTO>[] listener = new ListChangeListener[1];

            listener[0] = change -> {
                if (!architectureComboBox.getItems().isEmpty()) {
                    for (ArchitectureDTO architectureDTO : architectureComboBox.getItems()) {
                        if (chosenArchitecture.equals(architectureDTO.getRepresentation())) {
                            architectureComboBox.getSelectionModel().select(architectureDTO);
                            break;
                        }
                    }
                    architectureComboBox.getItems().removeListener(listener[0]); // ✅ ככה עובד
                }
            };

            architectureComboBox.getItems().addListener(listener[0]);
        }

    }
}
