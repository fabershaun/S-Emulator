package components.mainApp;

import components.topToolBar.ExpansionCollapseModel;
import dto.ProgramDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import components.debuggerExecutionMenu.DebuggerExecutionMenuController;
import components.instructionsTable.InstructionsTableController;
import components.loadFile.LoadFileController;
import components.topToolBar.TopToolBarController;

import java.nio.file.Path;


public class MainAppController {

    private Engine engine;

    Long[] inputs;
    @FXML private HBox loadFile;
    @FXML private LoadFileController loadFileController;    // must: field name = fx:id + "Controller"
    @FXML private HBox topToolBar;
    @FXML private TopToolBarController topToolBarController;
    @FXML private TableView<?> instructionsTable;
    @FXML private InstructionsTableController instructionsTableController;
    @FXML private Label summaryLineLabel;
    @FXML private VBox debuggerExecutionMenu;
    @FXML private DebuggerExecutionMenuController debuggerExecutionMenuController;

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
            instructionsTableController != null &&
            debuggerExecutionMenuController != null
        ) {
            initializeSubComponents();
        }
    }

    private void initializeSubComponents() {
        setMainControllerForSubcomponents();

        degreeModel.setProgram(currentProgramProperty.get());
        currentProgramProperty.addListener((observableValue, oldProgram, newProgram) -> degreeModel.setProgram(newProgram));
        topToolBarController.setModel(degreeModel);

        setPropertiesForSubcomponents();
        initializeBindingsForSubcomponents();
        initializeListenersForSubcomponents();
    }

    private void setMainControllerForSubcomponents() {
        loadFileController.setMainController(this);
        topToolBarController.setMainController(this);
        instructionsTableController.setMainController(this);
        debuggerExecutionMenuController.setMainController(this);
    }

    private void setPropertiesForSubcomponents() {
        loadFileController.setProperty(selectedFilePath, currentProgramProperty);
        instructionsTableController.setProperty(currentProgramProperty);
    }

    private void initializeListenersForSubcomponents() {
        instructionsTableController.initializeListener();
    }

    private void initializeBindingsForSubcomponents() {
        loadFileController.initializeBindings();
    }

    public void loadNewFile(Path xmlPath) throws EngineLoadException {
        engine.loadProgram(xmlPath);
        currentProgramProperty.set(engine.getProgram());
        degreeModel.setMaxDegree(engine.getMaxDegree());
        degreeModel.setCurrentDegree(engine.getCurrentDegree());
    }

    public ProgramDTO getCurrentProgram() {
        return engine.getProgram();
    }

    public void jumpToDegree(int target) throws EngineLoadException {
        int maxDegree = engine.getMaxDegree();
        int currentDegree = engine.getCurrentDegree();
        int safeTargetDegree = Math.max(0, Math.min(target, maxDegree));     // Clamp the requested degree to a valid range [0, maxDegree]
        if (safeTargetDegree == currentDegree) return;                       // No work needed if we are already at the requested degree

        try {
            ProgramDTO programByDegree = engine.getExpandedProgram(safeTargetDegree);                    // Execute the engine at the requested degree

            currentProgramProperty.set(programByDegree);
            degreeModel.setMaxDegree(engine.getMaxDegree());          // Sync the view model so UI bindings update label and combo boxes
            degreeModel.setCurrentDegree(safeTargetDegree);

        } catch (EngineLoadException e) {
            e.printStackTrace();            // TODO: change
        }
    }

    public void switchToProgram() {
    }

    public void switchToFunction() {
    }

}
