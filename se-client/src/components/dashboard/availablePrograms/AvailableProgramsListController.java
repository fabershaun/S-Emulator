package components.dashboard.availablePrograms;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AvailableProgramsListController implements Closeable {

    private Timer timer;
    private TimerTask listRefresher;
    private final IntegerProperty totalProgramsProperty;
    private final ObjectProperty<?> selectedProgramProperty = new SimpleObjectProperty<>();

    @FXML private Label programListLabel;
    @FXML private TableView<?> availableProgramTable;
    @FXML private TableColumn<?, ?> colAverageCreditsCost;
    @FXML private TableColumn<?, ?> colInstructionsAmount;
    @FXML private TableColumn<?, ?> colMaxDegree;
    @FXML private TableColumn<?, ?> colProgramName;
    @FXML private TableColumn<?, ?> colTimesPlayed;
    @FXML private TableColumn<?, ?> colUserUploaded;
    @FXML private Button executeProgramsButton;

    public AvailableProgramsListController() {
        totalProgramsProperty = new SimpleIntegerProperty();
    }

    @FXML
    public void initialize() {
        programListLabel.textProperty().bind(Bindings.concat("Available programs: (", totalProgramsProperty.asString(), ")"));

        availableProgramTable.getSelectionModel().selectedItemProperty().addListener((obs, oldProg, newProgramSelected) -> {
            selectedProgramProperty.set(newProgramSelected); // can be null if cleared
        });
    }

    private void updateProgramsList(List<String> usersList) {
        Platform.runLater(() -> {
            String currentSelection = usersListView.getSelectionModel().getSelectedItem();

            // Update list items
            ObservableList<String> items = usersListView.getItems();
            items.setAll(usersList);

            // Restore selection if still exists in the new list
            if (currentSelection != null && usersList.contains(currentSelection)) {
                usersListView.getSelectionModel().select(currentSelection);
            }

            // Update count
            totalUsers.set(usersList.size());
        });
    }

    @FXML
    void onExecuteProgramButtonClicked() {

    }

    @Override
    public void close() throws IOException {

    }
}
