package subComponents.expandAndCollapseComponent;

import subComponents.fullApp.FullAppController;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class ExpandAndCollapseController {
    private FullAppController mainController;

    public void setMainController(FullAppController mainController) {
        this.mainController = mainController;
    }

    @FXML private ToggleButton ProgramTB;
    @FXML private ToggleButton functionTB;
    @FXML private ToggleButton collapseTB;
    @FXML private ToggleButton expandTB;

    @FXML private ToggleGroup expandCollapseGroup;
    @FXML private ToggleGroup programFunctionGroup;


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
                mainController.expandOneStep();
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