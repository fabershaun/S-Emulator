package components.mainApp;

import components.chainInstructionTable.ChainInstructionsTableController;
import components.debuggerExecutionMenu.RunMode;
import components.history.HistoryController;
import components.mainInstructionsTable.MainInstructionsTableController;
import components.summaryLineOfMainInstructionsTable.SummaryLineController;
import components.topToolBar.ExpansionCollapseModel;
import components.topToolBar.HighlightSelectionModel;
import components.topToolBar.ProgramSelectorModel;
import dto.InstructionDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import components.debuggerExecutionMenu.DebuggerExecutionMenuController;
import components.loadFile.LoadFileController;
import components.topToolBar.TopToolBarController;
import javafx.stage.Stage;
import javafx.stage.Window;
import tasks.ExpandProgramTask;
import tasks.LoadProgramTask;
import tasks.ProgramRunTask;

import java.nio.file.Path;
import java.util.List;

import static components.loadFile.LoadFileController.handleTaskFailure;
import static components.loadFile.LoadFileController.showProgressDialog;

public class MainAppController {

    private Engine engine;

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

    private final StringProperty selectedFilePath = new SimpleStringProperty();
    private final ObjectProperty<ProgramDTO> mainProgramLoadedProperty = new SimpleObjectProperty<>(null);
    private final ObjectProperty<ProgramDTO> selectedProgramProperty = new SimpleObjectProperty<>(null);
    private final ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty = new SimpleObjectProperty<>(null);
    private final StringProperty programOrFunctionProperty = new SimpleStringProperty();

    private final ExpansionCollapseModel degreeModel = new ExpansionCollapseModel();
    private final HighlightSelectionModel highlightSelectionModel = new HighlightSelectionModel();
    private final ProgramSelectorModel programSelectorModel = new ProgramSelectorModel();

    public void setEngine(EngineImpl engine) {
        this.engine = engine;
    }

    @FXML
    public void initialize() {                          // We need that the subcomponents will know the main controller (FullAppController)
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

    private void initializeListeners() {
        selectedProgramProperty.addListener((obs, oldProg, newProg) -> {
            if (newProg != null) {
                degreeModel.setCurrentDegree(0);
                highlightSelectionModel.clearSelection();
                mainInstructionsTableController.fillTable(newProg.getInstructions());
                debuggerExecutionMenuController.clearInputs();
                historyMenuController.clearHistory();
                chainInstructionTableController.clearChainTable();
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

        mainProgramLoadedProperty.addListener((observableValue, oldProgram, newProgram) -> {
            programSelectorModel.setMainProgram(newProgram);
        });
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
        historyMenuController.setProperty(programAfterExecuteProperty, programOrFunctionProperty);
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
            selectedProgramProperty.set(loaded); // Default choose // todo: fix this line and line above
            selectedFilePath.set(xmlPath.toAbsolutePath().toString());
            programAfterExecuteProperty.set(null);
            try {
                degreeModel.setMaxDegree(engine.getMaxDegree());
            } catch (EngineLoadException e) {
                throw new RuntimeException(e);
            }

            degreeModel.setCurrentDegree(0);
        });

        loadProgramTask.setOnFailed(ev -> handleTaskFailure(loadProgramTask, progressStage));
        loadProgramTask.setOnCancelled(ev -> progressStage.close());

        new Thread(loadProgramTask, "loadProgram-thread").start();
    }

    public void jumpToDegree(int target) throws EngineLoadException {
        int maxDegree = engine.getMaxDegree();
        int safeTargetDegree = Math.max(0, Math.min(target, maxDegree));          // Clamp the requested degree to a valid range [0, maxDegree]
        String activeProgramName = getActiveProgramName();
        ExpandProgramTask expansionTask = new ExpandProgramTask(activeProgramName, engine, safeTargetDegree);

        expansionTask.setOnSucceeded(ev -> {
            ProgramDTO programByDegree = expansionTask.getValue();
            selectedProgramProperty.set(programByDegree);
            degreeModel.setMaxDegree(maxDegree);
            degreeModel.setCurrentDegree(safeTargetDegree);
        });

        new Thread(expansionTask, "expand-thread").start();
    }

    public List<ProgramDTO> getAllPrograms() {
        String mainProgramName = mainProgramLoadedProperty.get().getProgramName();
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

        ProgramRunTask runTask = new ProgramRunTask(activeProgramName, engine, RunMode.RUNNING, degree, inputs.toArray(new Long[0]));

        runTask.setOnSucceeded(ev -> {
            programAfterExecuteProperty.set(runTask.getValue());
        });

        new Thread(runTask, "runProgram-thread").start();
    }

    private String getActiveProgramName() {
        String chosenProgramName = programSelectorModel.getSelectedUserString();

        if (chosenProgramName == null) {
            chosenProgramName = engine.getMainProgram().getProgramName();
        }

        return chosenProgramName;
    }

    public List<ProgramExecutorDTO> getHistory() {
        return engine.getHistoryPerProgram(selectedProgramProperty.get().getProgramName());
    }

    // When re-run was pressed
    public void prepareForNewRun(int newDegree, List<Long> inputs) {
        degreeModel.setCurrentDegree(newDegree);
        debuggerExecutionMenuController.prepareForNewRun(inputs);
    }

    public ProgramDTO getChosenProgramByUserString(String userString) {
        return engine.getProgramByUserString(userString);
    }
}
