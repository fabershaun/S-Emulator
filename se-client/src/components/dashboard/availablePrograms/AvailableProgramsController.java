package components.dashboard.availablePrograms;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AvailableProgramsController {
    @FXML private TableView<?> availableProgramTable;
    @FXML private TableColumn<?, ?> colAverageCreditsCost;
    @FXML private TableColumn<?, ?> colInstructionsAmount;
    @FXML private TableColumn<?, ?> colMaxDegree;
    @FXML private TableColumn<?, ?> colProgramName;
    @FXML private TableColumn<?, ?> colTimesPlayed;
    @FXML private TableColumn<?, ?> colUserUploaded;

    @FXML private Button executeProgramsButton;

    @FXML
    public void initialize() {

    }

    @FXML
    void onExecuteProgramButtonClicked() {

    }
}
