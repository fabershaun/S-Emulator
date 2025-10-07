package components.dashboard.availablePrograms;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class AvailableProgramsController {
    @FXML private Button executeProgramsButton;
    @FXML private Label headerLabel;
    @FXML private ListView<String> itemsListView;

    @FXML
    public void initialize() {
        Label placeholderLabel = new Label("No history to display");
        placeholderLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic; -fx-alignment: center;");
        itemsListView.setPlaceholder(placeholderLabel);        // set placeholder dynamically
    }

    @FXML
    void onExecuteProgramsButtonClicked() {

    }
}
