package components.topToolBar;

import components.mainApp.MainAppController;
import dto.ProgramDTO;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class TopToolBarController {

    private MainAppController mainController;
    @FXML private ComboBox<?> collapseCB;
    @FXML private Label currentDegreeLabel;
    @FXML private ComboBox<?> expandCB;
    @FXML private ComboBox<?> highlightSelectionCB;
    @FXML private ComboBox<?> programFunctionSelectorCB;

    private ObjectProperty<ProgramDTO> currentProgramProperty;
    private IntegerProperty collapseProperty;
    private IntegerProperty expandProperty;

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setProperty(IntegerProperty collapseProperty, IntegerProperty expandProperty, ObjectProperty<ProgramDTO> currentProgramProperty) {
        this.currentProgramProperty = currentProgramProperty;
        this.collapseProperty = collapseProperty;
        this.expandProperty = expandProperty;
    }

}