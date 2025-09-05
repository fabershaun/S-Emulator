package subComponents.fullApp;

import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import subComponents.loadComponent.LoadFileController;

import java.nio.file.Path;


public class FullAppController {

    private final Engine engine = new EngineImpl();
    @FXML private HBox loadFileComponent;
    @FXML private LoadFileController loadFileComponentController;

    @FXML
    public void initialize() {                          // We need that the subcomponents will know the main controller (FullAppController)
        if (loadFileComponentController != null) {
            loadFileComponentController.setMainController(this);
        }
    }

    public void loadNewFile(Path xmlPath) throws EngineLoadException {
        engine.loadProgram(xmlPath);
    }

    public void collapseOneStep() {
        // לקבל את הדרגה הנוכחית של התוכנית
        // אם התוכנית היא מדרגה 0 לא לאפשר
        // להקטין את התוכנית בדרגה אחת
        // להחזיר את התוכנית
    }

    public void expandOneStep() {
    }

    public void switchToProgram() {

    }

    public void switchToFunction() {
    }

}
