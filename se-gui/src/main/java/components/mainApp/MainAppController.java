package components.mainApp;

import components.chainInstructionTable.ChainInstructionsTableController;
import components.history.HistoryController;
import components.mainInstructionsTable.MainInstructionsTableController;
import components.programCreation.ProgramCreationController;
import components.programCreation.ProgramCreationModel;
import components.summaryLineOfMainInstructionsTable.SummaryLineController;
import dto.v2.*;
import dto.v3.UserDTO;
import engine.logic.exceptions.EngineLoadException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import theme.ThemeManager;
import components.topToolBar.ExpansionCollapseModel;
import components.topToolBar.HighlightSelectionModel;
import components.topToolBar.ProgramSelectorModel;
import engine.Engine;
import engine.EngineImpl;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import components.debuggerExecutionMenu.DebuggerExecutionMenuController;
import components.loadFile.LoadFileController;
import components.topToolBar.TopToolBarController;
import javafx.stage.Stage;
import javafx.stage.Window;
import tasks.DebugResumeTask;
import tasks.ExpandProgramTask;
import tasks.LoadProgramTask;
import tasks.ProgramRunTask;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static components.history.HistoryController.convertToHistoryRows;
import static components.loadFile.LoadFileController.*;

public class MainAppController {

    private Engine engine;
    private DebugResumeTask currentDebugTask;

    @FXML private Label titleLabel;
    @FXML private HBox loadFile;
    @FXML private LoadFileController loadFileController;        // must: field name = fx:id + "Controller"
    @FXML private HBox topToolBar;
    @FXML private TopToolBarController topToolBarController;    // must: field name = fx:id + "Controller"
    @FXML private TableView<InstructionDTO> mainInstructionsTable;
    @FXML private MainInstructionsTableController mainInstructionsTableController;          // must: field name = fx:id + "Controller"
    @FXML private HBox summaryLine;
    @FXML private SummaryLineController summaryLineController;
    @FXML private TableView<InstructionDTO> chainInstructionTable;
    @FXML private ChainInstructionsTableController chainInstructionTableController;    // must: field name = fx:id + "Controller"
    @FXML private VBox debuggerExecutionMenu;
    @FXML private DebuggerExecutionMenuController debuggerExecutionMenuController;  // must: field name = fx:id + "Controller"
    @FXML private VBox historyMenu;
    @FXML private HistoryController historyMenuController;   // must: field name = fx:id + "Controller"
    @FXML private Button settingsButton;

    ContextMenu settingsMenu;

    private final StringProperty selectedFilePath = new SimpleStringProperty();
    private final ObjectProperty<ProgramDTO> mainProgramLoadedProperty = new SimpleObjectProperty<>(null);
    private final ObjectProperty<ProgramDTO> selectedProgramProperty = new SimpleObjectProperty<>(null);
    private final ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty = new SimpleObjectProperty<>(null);

    private final ExpansionCollapseModel degreeModel = new ExpansionCollapseModel();
    private final HighlightSelectionModel highlightSelectionModel = new HighlightSelectionModel();
    private final ProgramSelectorModel programSelectorModel = new ProgramSelectorModel();

    public void setEngine(EngineImpl engine) {
        this.engine = engine;
    }

    @FXML
    public void initialize() {            // We need that the subcomponents will know the main controller (FullAppController)
        initializeSettingsMenu();         // Initialize the settings menu

        // Apply theme when scene is available
        Platform.runLater(() -> {
            Scene scene = settingsButton.getScene();
            if (scene != null) {
                ThemeManager.getInstance().applyTheme(scene, "default");
            }
        });

        if (
            loadFileController != null &&
            topToolBarController != null &&
            mainInstructionsTableController != null &&
            summaryLineController != null &&
            chainInstructionTableController != null &&
            debuggerExecutionMenuController != null &&
            historyMenuController != null
        ) {
            initializeSubComponents();
            initializeListeners();
        }
    }

    private void initializeSettingsMenu() {
        settingsMenu = new ContextMenu();

        // Add theme options directly to the settings menu
        for (String themeName : ThemeManager.getInstance().getAvailableThemes()) {
            MenuItem themeItem = new MenuItem(themeName);
            themeItem.setOnAction(e -> {
                Scene scene = settingsButton.getScene();
                if (scene != null) {
                    ThemeManager.getInstance().applyTheme(scene, themeName);
                }
            });
            settingsMenu.getItems().add(themeItem);
        }
    }

    private void initializeListeners() {
        programSelectorModel.selectedUserStringProperty().addListener((obs, oldVal, newSelectedUserString) -> {
            if (newSelectedUserString != null) {
                ProgramDTO selectedProgram = engine.getProgramDTOByUserString(newSelectedUserString);

                selectedProgramProperty.set(selectedProgram);
                highlightSelectionModel.clearSelection();
                debuggerExecutionMenuController.enterProgramReady();
                historyMenuController.fillHistoryTable(getHistory());
                degreeModel.setCurrentDegree(0);
                degreeModel.setMaxDegree(engine.getMaxDegree(selectedProgram.getProgramName()));
            } else {
                selectedProgramProperty.set(null);
            }
        });
    }

    private void initializeSubComponents() {
        setMainControllerForSubcomponents();

        initializeSubModels();
        setModelsForSubComponents();

        setPropertiesForSubcomponents();
        initializeBindingsForSubcomponents();
        initializeListenersForSubcomponents();
    }

    private void setModelsForSubComponents() {
        topToolBarController.setModels(degreeModel, highlightSelectionModel, programSelectorModel);
        mainInstructionsTableController.setModels(highlightSelectionModel, programSelectorModel);
    }

    private void initializeSubModels() {
        degreeModel.setProgram(selectedProgramProperty.get());
        highlightSelectionModel.setProgram(selectedProgramProperty.get());

        selectedProgramProperty.addListener((observableValue, oldProgram, newProgram) -> {
            degreeModel.setProgram(newProgram);
            highlightSelectionModel.setProgram(newProgram);
        });

        programSelectorModel.setMainProgram(mainProgramLoadedProperty.get());
        programSelectorModel.setSelectedProgram(selectedProgramProperty.get());

        mainProgramLoadedProperty.addListener((observableValue, oldProgram, newProgram) -> programSelectorModel.setMainProgram(newProgram));
    }

    private void setMainControllerForSubcomponents() {
        loadFileController.setMainController(this);
        topToolBarController.setMainController(this);
        mainInstructionsTableController.setMainController(this);
        debuggerExecutionMenuController.setMainController(this);
        historyMenuController.setMainController(this);
    }

    private void setPropertiesForSubcomponents() {
        loadFileController.setProperty(selectedFilePath);
        mainInstructionsTableController.setProperty(selectedProgramProperty);
        summaryLineController.setProperty(selectedProgramProperty);
        debuggerExecutionMenuController.setProperty(selectedProgramProperty, programAfterExecuteProperty);
        historyMenuController.setProperty(programAfterExecuteProperty);
    }

    private void initializeListenersForSubcomponents() {
        mainInstructionsTableController.initializeListeners();
        debuggerExecutionMenuController.initializeListeners();
        historyMenuController.initializeListeners();
    }

    private void initializeBindingsForSubcomponents() {
        loadFileController.initializeBindings();
        summaryLineController.initializeBindings();
    }

    public void loadNewFile(Path xmlPath, Window ownerWindow) {
        LoadProgramTask loadProgramTask = new LoadProgramTask(engine, xmlPath);

        Stage progressStage = showProgressDialog(loadProgramTask, ownerWindow);

        loadProgramTask.setOnSucceeded(ev -> {
            progressStage.close();
            ProgramDTO loaded = loadProgramTask.getValue();

            mainProgramLoadedProperty.set(loaded);
            selectedProgramProperty.set(loaded); // Default choose
            selectedFilePath.set(xmlPath.toAbsolutePath().toString());
            programAfterExecuteProperty.set(null);
            degreeModel.setMaxDegree(engine.getMaxDegree(selectedProgramProperty.get().getProgramName()));
            degreeModel.setCurrentDegree(0);
            historyMenuController.clearHistoryTable();  // Clear the table in case of debug -> load
            debuggerExecutionMenuController.clearVariableTableAndResetCycles();   // Clear the table in case of debug -> load
        });

        loadProgramTask.setOnFailed(ev -> handleLoadTaskFailure(loadProgramTask, progressStage));
        loadProgramTask.setOnCancelled(ev -> progressStage.close());

        new Thread(loadProgramTask, "loadProgram-thread").start();
    }

    public void jumpToDegree(int target) {
        int maxDegree = engine.getMaxDegree(selectedProgramProperty.get().getProgramName());
        int safeTargetDegree = Math.max(0, Math.min(target, maxDegree));          // Clamp the requested degree to a valid range [0, maxDegree]
        String activeProgramName = getActiveProgramName();
        ExpandProgramTask expansionTask = new ExpandProgramTask(activeProgramName, engine, safeTargetDegree);

        expansionTask.setOnSucceeded(ev -> {
            ProgramDTO programByDegree = expansionTask.getValue();
            selectedProgramProperty.set(programByDegree);
            degreeModel.setMaxDegree(maxDegree);
            degreeModel.setCurrentDegree(safeTargetDegree);
        });

        expansionTask.setOnFailed(ev -> handleTaskFailure(expansionTask, "Expand failed"));

        new Thread(expansionTask, "expand-thread").start();
    }

    public List<ProgramDTO> getAllPrograms() {
        return engine.getAllPrograms();
    }

    public void onInstructionSelected(InstructionDTO selectedInstruction) {
        int instructionNumber = selectedInstruction.getInstructionNumber();
        List<InstructionDTO> selectedInstructionChain = selectedProgramProperty.get().getExpandedProgram().get(instructionNumber - 1); // -1 because we started the count from 0
        chainInstructionTableController.fillTable(selectedInstructionChain);
    }

    public void onInstructionDeselected() {
        chainInstructionTableController.clearChainTable();
    }

    public void runProgram(List<Long> inputs) {
        int degree = degreeModel.currentDegreeProperty().get();
        String activeProgramName = getActiveProgramName();

        ProgramRunTask runTask = new ProgramRunTask(activeProgramName, engine, degree, inputs.toArray(new Long[0]));

        runTask.setOnSucceeded(ev -> programAfterExecuteProperty.set(runTask.getValue()));

        runTask.setOnFailed(ev -> handleTaskFailure(runTask, "Run Failed"));

        new Thread(runTask, "runProgram-thread").start();
    }

    private String getActiveProgramName() {
        String chosenUserString = programSelectorModel.getSelectedUserString();
        String chosenProgramName = engine.getProgramNameByUserString(chosenUserString);

        if (chosenProgramName == null) {    // If the user didnt select something
            chosenProgramName = mainProgramLoadedProperty.get().getProgramName();
        }

        return chosenProgramName;
    }

    public List<HistoryRowV2DTO> getHistory() {
        return convertToHistoryRows(engine.getHistoryV2PerProgram(selectedProgramProperty.get().getProgramName()));
    }

    // When re-run was pressed
    public void prepareForNewRun(int newDegree, List<Long> inputs) {
        degreeModel.setCurrentDegree(newDegree);
        debuggerExecutionMenuController.prepareForNewRun(inputs);
    }

    public ProgramDTO getChosenProgramByUserString(String userString) {
        return engine.getProgramDTOByUserString(userString);
    }

    public void initializeDebugger(List<Long> inputValues) {
        engine.initializeDebugger(getActiveProgramName(), ProgramExecutorDTO.DEFAULT_ARCHITECTURE, degreeModel.currentDegreeProperty().get(), inputValues, UserDTO.DEFAULT_NAME);
    }

    public void debugStop() {
        engine.stopDebugPress(UserDTO.DEFAULT_NAME);

        if (currentDebugTask != null && currentDebugTask.isRunning()) { // kill the running thread
            currentDebugTask.cancel(true);
        }
    }

    public void debugResume(Consumer<DebugDTO> onComplete) {
        List<Boolean> breakPoints = mainInstructionsTableController.getBreakPoints();

        currentDebugTask = new DebugResumeTask(engine, getActiveProgramName(), breakPoints);

        currentDebugTask.setOnSucceeded(ev -> {
            DebugDTO debugStep = currentDebugTask.getValue();

            if (debugStep.hasMoreInstructions()) {
                updateControllerAfterDebugStep(debugStep);
            }

            onComplete.accept(debugStep);
        });

        currentDebugTask.setOnFailed(ev -> handleTaskFailure(currentDebugTask, "Debug Resume Failed"));

        Thread thread = new Thread(currentDebugTask, "debugResume-thread");
        thread.setDaemon(true);
        thread.start();
    }

    public DebugDTO debugStepOver() {
        DebugDTO debugStep = engine.getProgramAfterStepOver(UserDTO.DEFAULT_NAME);
        updateControllerAfterDebugStep(debugStep);

        return debugStep;
    }

    public DebugDTO debugStepBack() {
        DebugDTO debugStep = engine.getProgramAfterStepBack(UserDTO.DEFAULT_NAME);
        updateControllerAfterDebugStep(debugStep);

        return debugStep;
    }

    private void updateControllerAfterDebugStep(DebugDTO debugStep) {
        mainInstructionsTableController.highlightLineDebugMode(debugStep.getNextInstructionNumber());  // Highlight line on table instructions
        topToolBarController.setComponentsDisabled(true);
        historyMenuController.setHistoryButtonsDisabled(true);
    }

    public void finishDebug() {
        historyMenuController.updateHistoryTableManual();   // Manual update

        mainInstructionsTableController.turnOffHighlighting();
        topToolBarController.setComponentsDisabled(false);
        historyMenuController.setHistoryButtonsDisabled(false);
    }

    public static void handleTaskFailure(javafx.concurrent.Task<?> task, String title) {
        Throwable taskException = task.getException();
        String msg;

        if (taskException instanceof EngineLoadException) {
            msg = taskException.getMessage();
        } else if (taskException != null) {
            msg = taskException.getMessage() != null ? taskException.getMessage() : taskException.toString();
        } else {
            msg = "Unknown  error";
        }

        showEngineError(title, msg);
    }

    public void clearHistorySelection() {
        historyMenuController.clearHistoryTableRowSelection();
    }

    public void disableHistoryAndToolBarComponents(Boolean disable) {
        historyMenuController.setHistoryButtonsDisabled(disable);
        topToolBarController.setComponentsDisabled(disable);
    }

    public void EnterDebugMode() {
        mainInstructionsTableController.highlightLineDebugMode(0);  // Highlight the first line on table instructions
        disableHistoryAndToolBarComponents(true);
    }

    @FXML
    public void onSettingsClicked() {
        // Show settings menu when settings button is clicked
        settingsMenu.show(settingsButton, Side.BOTTOM, 0, 0);
    }

    @FXML
    public void onCreateProgramClicked() {
        try {
            // Load the FXML of the Program Creation window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/programCreation/programCreation.fxml"));
            Parent root = loader.load();

            // Get the controller of ProgramCreation
            ProgramCreationController creationController = loader.getController();

            // Pass the model
            ProgramCreationModel model = new ProgramCreationModel();
            creationController.setModel(model);
            creationController.setMainController(this);
            creationController.setOwnerWindow(loadFile.getScene().getWindow()); // needed for the upload back

            // Create a new window (Stage)
            Stage stage = new Stage();
            stage.setTitle("Program Builder");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Prevents interaction with main window until closed (optional)
            stage.show();

        } catch (IOException e) {
            e.printStackTrace(); // Or show an alert to the user
        }
    }
}
