package components.mainApp;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import components.debuggerExecutionMenu.DebuggerExecutionMenuController;
import components.instructionsTable.InstructionsTableController;
import components.loadFile.LoadFileController;
import components.topToolBar.TopToolBarController;

import java.nio.file.Path;


public class MainAppController {

    private final AppState state = new AppState();
    private Engine engine;

    Long[] inputs;
    @FXML private HBox loadFile;
    @FXML private LoadFileController loadFileController;    // must: field name = fx:id + "Controller"
    @FXML private HBox topToolBar;
    @FXML private TopToolBarController topToolBarController;
    @FXML private TableView<?> instructionsTable;
    @FXML private InstructionsTableController instructionsTableController;
    @FXML private TextField summaryLineTF;
    @FXML private TextField summaryHistoryLineTF;
    @FXML private VBox debuggerExecutionMenu;
    @FXML private DebuggerExecutionMenuController debuggerExecutionMenuController;


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
            setMainControllerForSubcomponents();
            setStateForSubcomponents();

        }
    }

    private void setMainControllerForSubcomponents() {
        loadFileController.setMainController(this);
        topToolBarController.setMainController(this);
        instructionsTableController.setMainController(this);
        debuggerExecutionMenuController.setMainController(this);
    }

    private void setStateForSubcomponents() {
        loadFileController.setState(state);
        topToolBarController.setState(state);
        instructionsTableController.setState(state);
        debuggerExecutionMenuController.setState(state);
    }

    public void loadNewFile(Path xmlPath) throws EngineLoadException {
        engine.loadProgram(xmlPath);
    }

    public ProgramDTO getCurrentProgram() {
        return engine.getProgram();
    }

    public void collapseOneStep() {
        int currentDegreeLoaded = engine.getCurrentDegree();

        if (currentDegreeLoaded > 0) {
            engine.runProgram(--currentDegreeLoaded, inputs);   // TODO: initialize inputs
            ProgramExecutorDTO programExecutorDTO = engine.getProgramAfterRun();

            //TODO: להציג ברכיב הפקודות את התוכנית המורחבת
            //TODO: להציג ברכיב הפקודה המורחבת את חלקי הפקודה המורחבת
            //TODO: לעדכן את המשתנים בערכים השונים
        }
    }

    public void expandOneStep() throws EngineLoadException {
        int currentDegreeLoaded = engine.getCurrentDegree();

        if (currentDegreeLoaded < engine.getMaxDegree()) {
            engine.runProgram(++currentDegreeLoaded, inputs);   // TODO: initialize inputs
            ProgramExecutorDTO programExecutorDTO = engine.getProgramAfterRun();

            //TODO: להציג ברכיב הפקודות את התוכנית המורחבת
            //TODO: להציג ברכיב הפקודה המורחבת את חלקי הפקודה המורחבת
            //TODO: לעדכן את המשתנים בערכים השונים
        }
    }

    public void switchToProgram() {
    }

    public void switchToFunction() {
    }

}
