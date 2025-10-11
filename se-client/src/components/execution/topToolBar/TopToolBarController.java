package components.execution.topToolBar;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class TopToolBarController {

    @FXML private ComboBox<String> programSelectorCB;
    @FXML private ComboBox<Integer> collapseCB;
    @FXML private Label currentDegreeLabel;
    @FXML private ComboBox<Integer> expandCB;
    @FXML private ComboBox<String> highlightSelectionCB;
}
