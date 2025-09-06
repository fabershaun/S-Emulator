package subComponents.fullApp;

import dto.ProgramExecutorDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import subComponents.loadFile.LoadFileController;
import subComponents.topToolBar.TopToolBarController;

import java.nio.file.Path;


public class FullAppController {

    private final Engine engine = new EngineImpl();
    Long[] inputs;
    @FXML private HBox loadFile;
    @FXML private LoadFileController loadFileController;    // must: field name = fx:id + "Controller"
    @FXML private HBox topToolBar;
    @FXML private TopToolBarController topToolBarController;

    @FXML
    public void initialize() {                          // We need that the subcomponents will know the main controller (FullAppController)
        if (
                loadFileController != null &&
                topToolBarController != null
        ) {
            loadFileController.setMainController(this);
            topToolBarController.setMainController(this);
        }
    }

    public void loadNewFile(Path xmlPath) throws EngineLoadException {
        engine.loadProgram(xmlPath);
    }

    public void collapseOneStep() {
        int currentDegreeLoaded = engine.getCurrentDegree();

        if (currentDegreeLoaded > 0) {
            engine.runProgram(--currentDegreeLoaded, inputs);   // TODO: initialize inputs
            ProgramExecutorDTO programExecutorDTO = engine.getProgramAfterRun();

            // להציג ברכיב הפקודות את התוכנית המורחבת
            // להציג ברכיב הפקודה המורחבת את חלקי הפקודה המורחבת
            // לעדכן את המשתנים בערכים השונים
        }
    }

    public void expandOneStep() throws EngineLoadException {
        int currentDegreeLoaded = engine.getCurrentDegree();

        if (currentDegreeLoaded < engine.getMaxDegree()) {
            engine.runProgram(++currentDegreeLoaded, inputs);   // TODO: initialize inputs
            ProgramExecutorDTO programExecutorDTO = engine.getProgramAfterRun();

            // להציג ברכיב הפקודות את התוכנית המורחבת
            // להציג ברכיב הפקודה המורחבת את חלקי הפקודה המורחבת
            // לעדכן את המשתנים בערכים השונים
        }
    }

    public void switchToProgram() {

    }

    public void switchToFunction() {
    }

}
