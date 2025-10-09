package components.dashboard.availablePrograms;

import dto.v3.MainProgramDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.Closeable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static utils.Constants.REFRESH_RATE;

public class AvailableProgramsListController implements Closeable {

    private Timer timer;
    private TimerTask programsListRefresher;
    private final IntegerProperty totalProgramsProperty;
    private final ObjectProperty<MainProgramDTO> selectedProgramProperty = new SimpleObjectProperty<>();

    @FXML private Label programListLabel;
    @FXML private TableView<MainProgramDTO> availableProgramTableView;
    @FXML private TableColumn<MainProgramDTO, String> colProgramName;
    @FXML private TableColumn<MainProgramDTO, String> colUserUploaded;
    @FXML private TableColumn<MainProgramDTO, Number> colInstructionsAmount;
    @FXML private TableColumn<MainProgramDTO, Number> colMaxDegree;
    @FXML private TableColumn<MainProgramDTO, Number> colTimesPlayed;
    @FXML private TableColumn<MainProgramDTO, Number> colAverageCreditsCost;
    @FXML private Button executeProgramsButton;

    public AvailableProgramsListController() {
        totalProgramsProperty = new SimpleIntegerProperty();
    }

    @FXML
    public void initialize() {
        programListLabel.textProperty().bind(Bindings.concat("Available programs: (", totalProgramsProperty.asString(), ")"));

        colProgramName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProgramName()));
        colUserUploaded.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUploaderName()));
        colInstructionsAmount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getInstructionsAmount()));
        colMaxDegree.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getMaxDegree()));
        colTimesPlayed.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTimesPlayed()));
        colAverageCreditsCost.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getAverageCreditCost()));
    }

    public void initListeners() {
        availableProgramTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldProg, newProgramSelected) -> {
            if (newProgramSelected == null) {
                executeProgramsButton.setDisable(true);
                selectedProgramProperty.set(null);
            } else {
                executeProgramsButton.setDisable(false);
                selectedProgramProperty.set(newProgramSelected);
            }
        });
    }

    private void updateProgramsList(List<MainProgramDTO> programsList) {
        Platform.runLater(() -> {
            if (availableProgramTableView.getItems().equals(programsList)) {
                return; // No change, skip refresh
            }

            MainProgramDTO currentSelection = availableProgramTableView.getSelectionModel().getSelectedItem();

            // Update list items
            ObservableList<MainProgramDTO> items = availableProgramTableView.getItems();
            items.setAll(programsList);

            // Restore selection if still exists in the new list
            if (currentSelection != null && programsList.contains(currentSelection)) {
                availableProgramTableView.getSelectionModel().select(currentSelection);
            }

            // Update count
            totalProgramsProperty.set(programsList.size());
        });
    }

    public void startListRefresher() {
        programsListRefresher = new ProgramsListRefresher(this::updateProgramsList);
        timer = new Timer();
        timer.schedule(programsListRefresher, 0, REFRESH_RATE);
    }

    // TODO: WRITE
    @FXML
    void onExecuteProgramButtonClicked() {
        String mainProgramSelectedName = selectedProgramProperty.get().getProgramName();


    }

    @Override
    public void close() {
        availableProgramTableView.getItems().clear();
        totalProgramsProperty.setValue(0);
        if (programsListRefresher != null && timer != null) {
            programsListRefresher.cancel();
            timer.cancel();
        }
    }
}
