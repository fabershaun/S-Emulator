package components.dashboard;

import components.loadFileClient.LoadFileController;
import components.mainApp.MainAppController;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class DashboardController {

    private MainAppController mainAppController;

    @FXML private BorderPane loadFile;
    @FXML private LoadFileController loadFileController;        // must: field name = fx:id + "Controller"


    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }
}
