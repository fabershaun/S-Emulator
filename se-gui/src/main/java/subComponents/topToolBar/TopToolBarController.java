package subComponents.topToolBar;

import exceptions.EngineLoadException;
import subComponents.fullApp.FullAppController;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class TopToolBarController {

    private FullAppController mainController;
    @FXML private ToggleButton ProgramTB;
    @FXML private ToggleButton functionTB;
    @FXML private ToggleButton collapseTB;
    @FXML private ToggleButton expandTB;
    @FXML private ToggleGroup expandCollapseGroup;
    @FXML private ToggleGroup programFunctionGroup;


    public void setMainController(FullAppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        expandCollapseGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) return;

            if (newToggle == collapseTB) {
                handleExpandCollapse("COLLAPSE");
            } else if (newToggle == expandTB) {
                handleExpandCollapse("EXPAND");
            }
        });

        programFunctionGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) return;

            if (newToggle == ProgramTB) {
                handleProgramFunction("PROGRAM");
            } else if (newToggle == functionTB) {
                handleProgramFunction("FUNCTION");
            }
        });
    }

    private void handleExpandCollapse(String mode) {
        if (mainController != null) {
            if (mode.equals("COLLAPSE")) {
                mainController.collapseOneStep();
            } else {
                try {
                    mainController.expandOneStep();
                } catch (EngineLoadException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handleProgramFunction(String type) {
        if (mainController != null) {
            if (type.equals("PROGRAM")) {
                mainController.switchToProgram();
            } else {
                mainController.switchToFunction();
            }
        }
    }
}