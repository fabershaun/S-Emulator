package components.mainApp;

import components.chainInstructionTable.ChainInstructionsTableController;
import components.mainInstructionsTable.MainInstructionsTableController;
import components.summaryLineOfMainInstructionsTable.SummaryLineController;
import components.topToolBar.ExpansionCollapseModel;
import dto.InstructionDTO;
import dto.ProgramDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import components.debuggerExecutionMenu.DebuggerExecutionMenuController;
import components.loadFile.LoadFileController;
import components.topToolBar.TopToolBarController;
import tasks.ExpandProgramTask;

import java.nio.file.Path;
import java.util.List;

public class MainAppController {

    private Engine engine;

    Long[] inputs;
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

    private final StringProperty selectedFilePath = new SimpleStringProperty();
    private final ObjectProperty<ProgramDTO> currentProgramProperty = new SimpleObjectProperty<>(null);
    private final StringProperty programOrFunctionProperty = new SimpleStringProperty();

    private final ExpansionCollapseModel degreeModel = new ExpansionCollapseModel();


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
            debuggerExecutionMenuController != null
        ) {
            initializeSubComponents();
        }
    }

    private void initializeSubComponents() {
        setMainControllerForSubcomponents();

        degreeModel.setProgram(currentProgramProperty.get());
        currentProgramProperty.addListener((observableValue, oldProgram, newProgram) -> {
            degreeModel.setProgram(newProgram);
        }); 
        
        topToolBarController.setModel(degreeModel);

        setPropertiesForSubcomponents();
        initializeBindingsForSubcomponents();
        initializeListenersForSubcomponents();
    }


    private void setMainControllerForSubcomponents() {
        loadFileController.setMainController(this);
        topToolBarController.setMainController(this);
        mainInstructionsTableController.setMainController(this);
        summaryLineController.setMainController(this);
        chainInstructionTableController.setMainController(this);
        debuggerExecutionMenuController.setMainController(this);
    }

    private void setPropertiesForSubcomponents() {
        loadFileController.setProperty(selectedFilePath, currentProgramProperty);
        mainInstructionsTableController.setProperty(currentProgramProperty);
        summaryLineController.setProperty(currentProgramProperty);
    }

    private void initializeListenersForSubcomponents() {
        mainInstructionsTableController.initializeListeners();
    }

    private void initializeBindingsForSubcomponents() {
        loadFileController.initializeBindings();
        summaryLineController.initializeBindings();
    }

    public void loadNewFile(Path xmlPath) throws EngineLoadException {
        engine.loadProgram(xmlPath);
        currentProgramProperty.set(engine.getProgram());
        degreeModel.setMaxDegree(engine.getMaxDegree());
        degreeModel.setCurrentDegree(engine.getCurrentDegreeAfterRun());
    }

    public ProgramDTO getCurrentProgram() {
        return engine.getProgram();
    }

    public void jumpToDegree(int target) throws EngineLoadException {
        int maxDegree = engine.getMaxDegree();
        int safeTargetDegree = Math.max(0, Math.min(target, maxDegree));          // Clamp the requested degree to a valid range [0, maxDegree]

        ExpandProgramTask task = new ExpandProgramTask(engine, safeTargetDegree);

        bindTaskToUIComponents(task, () -> {
            ProgramDTO programByDegree = task.getValue();
            currentProgramProperty.set(programByDegree);
            degreeModel.setMaxDegree(maxDegree);
            degreeModel.setCurrentDegree(safeTargetDegree);
        });

        new Thread(task).start();
    }

    public void bindTaskToUIComponents(Task<?> task, Runnable onFinish) {

        task.valueProperty().addListener((obs, oldVal, newVal) -> {
            onFinish.run();
        });
    }

    public void onInstructionSelected(InstructionDTO selectedInstruction) {
        int instructionNumber = selectedInstruction.getInstructionNumber();
        List<InstructionDTO> selectedInstructionChain = currentProgramProperty.get().getExpandedProgram().get(instructionNumber - 1); // -1 because we started the count from 0
        chainInstructionTableController.fillTable(selectedInstructionChain);
    }

    public void onInstructionDeselected() {
        chainInstructionTableController.clearHistory();
    }
}
