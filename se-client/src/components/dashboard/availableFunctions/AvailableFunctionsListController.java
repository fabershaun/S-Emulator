package components.dashboard.availableFunctions;

import components.dashboard.mainDashboard.DashboardController;
import dto.v3.FunctionDTO;
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

import static utils.http.Constants.REFRESH_RATE;

public class AvailableFunctionsListController implements Closeable {

    private DashboardController dashboardController;
    private final IntegerProperty totalFunctionsProperty;
    private final ObjectProperty<FunctionDTO> selectedFunctionProperty = new SimpleObjectProperty<>();

    private TimerTask functionsListRefresher;
    private Timer timer;

    @FXML private Label functionListLabel;
    @FXML private TableView<FunctionDTO> availableFunctionTableView;
    @FXML private TableColumn<FunctionDTO, String> colFunctionName;
    @FXML private TableColumn<FunctionDTO, String> mainProgramName;
    @FXML private TableColumn<FunctionDTO, String> colUserUploaded;
    @FXML private TableColumn<FunctionDTO, Number> colInstructionsAmount;
    @FXML private TableColumn<FunctionDTO, Number> colMaxDegree;
    @FXML private Button executeProgramsButton;

    public AvailableFunctionsListController() {
        totalFunctionsProperty = new SimpleIntegerProperty();
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        functionListLabel.textProperty().bind(Bindings.concat("Available Functions: (", totalFunctionsProperty.asString(), ")"));

        colFunctionName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFunctionName()));
        mainProgramName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMainProgramName()));
        colInstructionsAmount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getInstructionsAmount()));
        colMaxDegree.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getMaxDegree()));
    }

    public void initListeners() {
        availableFunctionTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldProg, newProgramSelected) -> {
            if (newProgramSelected == null) {
                executeProgramsButton.setDisable(true);
                selectedFunctionProperty.set(null);
            } else {
                executeProgramsButton.setDisable(false);
                selectedFunctionProperty.set(newProgramSelected);
            }
        });
    }

    private void updateFunctionsList(List<FunctionDTO> functionsList) {
        Platform.runLater(() -> {
            if (availableFunctionTableView.getItems().equals(functionsList)) {
                return; // No change, skip refresh
            }

            FunctionDTO currentSelection = availableFunctionTableView.getSelectionModel().getSelectedItem();

            // Update list items
            ObservableList<FunctionDTO> items = availableFunctionTableView.getItems();
            items.setAll(functionsList);

            // Restore selection if still exists in the new list
            if (currentSelection != null) {
                String selectedName = currentSelection.getFunctionName();

                functionsList.stream()
                        .filter(p -> p.getFunctionName().equals(selectedName))
                        .findFirst()
                        .ifPresent(p -> availableFunctionTableView.getSelectionModel().select(p));
            }

            // Update count
            totalFunctionsProperty.set(functionsList.size());
        });
    }

    public void startListRefresher() {
        functionsListRefresher = new FunctionsListRefresher(this::updateFunctionsList);
        timer = new Timer();
        timer.schedule(functionsListRefresher, 0, REFRESH_RATE);
    }

    @FXML
    void onExecuteFunctionButtonClicked() {
        String functionSelectedName = selectedFunctionProperty.get().getFunctionName();
        dashboardController.switchToExecution(functionSelectedName);
    }

    @Override
    public void close() {
        availableFunctionTableView.getItems().clear();
        totalFunctionsProperty.setValue(0);
        if (functionsListRefresher != null && timer != null) {
            functionsListRefresher.cancel();
            timer.cancel();
        }
    }
}
