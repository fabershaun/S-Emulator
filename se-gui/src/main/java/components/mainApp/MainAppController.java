package components.mainApp;

import components.chainInstructionTable.ChainInstructionsTableController;
import components.debuggerExecutionMenu.RunMode;
import components.history.HistoryController;
import components.mainInstructionsTable.MainInstructionsTableController;
import components.summaryLineOfMainInstructionsTable.SummaryLineController;
import components.topToolBar.ExpansionCollapseModel;
import components.topToolBar.HighlightSelectionModel;
import components.topToolBar.ProgramAndFunctionsSelectorModel;
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
    private final ObjectProperty<ProgramDTO> currentLoadedProgramProperty = new SimpleObjectProperty<>(null);
    private final ObjectProperty<ProgramDTO> currentSelectedProgramProperty = new SimpleObjectProperty<>(null);
    private final ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty = new SimpleObjectProperty<>(null);
    private final StringProperty programOrFunctionProperty = new SimpleStringProperty();

    private final ExpansionCollapseModel degreeModel = new ExpansionCollapseModel();
    private final HighlightSelectionModel highlightSelectionModel = new HighlightSelectionModel();
    private final ProgramAndFunctionsSelectorModel programAndFunctionsSelectorModel = new ProgramAndFunctionsSelectorModel();

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
        }
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
        topToolBarController.setModels(degreeModel, highlightSelectionModel, programAndFunctionsSelectorModel);
        mainInstructionsTableController.setModels(highlightSelectionModel);
    }

    private void initializeSubModels() {
        degreeModel.setProgram(currentSelectedProgramProperty.get());
        currentSelectedProgramProperty.addListener((observableValue, oldProgram, newProgram) -> {
            degreeModel.setProgram(newProgram);
        });

        highlightSelectionModel.setProgram(currentSelectedProgramProperty.get());
        currentSelectedProgramProperty.addListener((observableValue, oldProgram, newProgram) -> {
            highlightSelectionModel.setProgram(newProgram);
        });

        programAndFunctionsSelectorModel.setProgram(currentLoadedProgramProperty.get());
        currentLoadedProgramProperty.addListener((observableValue, oldProgram, newProgram) -> {
            programAndFunctionsSelectorModel.setProgram(newProgram);
        });
    }

    private void setMainControllerForSubcomponents() {
        loadFileController.setMainController(this);
        topToolBarController.setMainController(this);
        mainInstructionsTableController.setMainController(this);
        chainInstructionTableController.setMainController(this);
        debuggerExecutionMenuController.setMainController(this);
        historyMenuController.setMainController(this);
    }

    private void setPropertiesForSubcomponents() {
        loadFileController.setProperty(selectedFilePath);
        mainInstructionsTableController.setProperty(currentSelectedProgramProperty);
        summaryLineController.setProperty(currentSelectedProgramProperty);
        debuggerExecutionMenuController.setProperty(currentSelectedProgramProperty, programAfterExecuteProperty);
        historyMenuController.setProperty(programAfterExecuteProperty, programOrFunctionProperty);
    }

    private void initializeListenersForSubcomponents() {
        mainInstructionsTableController.initializeListeners();
        debuggerExecutionMenuController.initializeListeners();
        debuggerExecutionMenuController.initializeListeners();

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
            currentLoadedProgramProperty.set(loaded);
            currentSelectedProgramProperty.set(loaded); // Default choose
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

        ExpandProgramTask expansionTask = new ExpandProgramTask(engine, safeTargetDegree);

        expansionTask.setOnSucceeded(ev -> {
            ProgramDTO programByDegree = expansionTask.getValue();
            currentSelectedProgramProperty.set(programByDegree);
            degreeModel.setMaxDegree(maxDegree);
            degreeModel.setCurrentDegree(safeTargetDegree);
        });

        new Thread(expansionTask, "expand-thread").start();
    }

    public List<ProgramDTO> getProgramAndFunctionsOfProgramList() {
        return engine.getSubProgramsOfProgram(currentLoadedProgramProperty.get().getProgramName());
    }

    public void onInstructionSelected(InstructionDTO selectedInstruction) {
        int instructionNumber = selectedInstruction.getInstructionNumber();
        List<InstructionDTO> selectedInstructionChain = currentSelectedProgramProperty.get().getExpandedProgram().get(instructionNumber - 1); // -1 because we started the count from 0
        chainInstructionTableController.fillTable(selectedInstructionChain);
    }

    public void onInstructionDeselected() {
        chainInstructionTableController.clearHistory();
    }

    public void runProgram(List<Long> inputs) {
        int degree = degreeModel.currentDegreeProperty().get();

        ProgramRunTask runTask = new ProgramRunTask(engine, RunMode.RUNNING, degree, inputs.toArray(new Long[0]));

        runTask.setOnSucceeded(ev -> {
            programAfterExecuteProperty.set(runTask.getValue());
        });
        
        new Thread(runTask, "runProgram-thread").start();
    }
}
