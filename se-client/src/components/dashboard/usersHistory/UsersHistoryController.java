package components.dashboard.usersHistory;

import components.dashboard.mainDashboard.DashboardController;
import components.dashboard.usersHistory.historyRowPopUp.HistoryRowPopUpController;
import dto.v2.HistoryRowDTO;
import dto.v2.ProgramExecutorDTO;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.Constants.*;

public class UsersHistoryController {

    private DashboardController dashboardController;
    private StringProperty selectedUserProperty;
    private StringProperty currentUserLoginProperty;

    private HistoryRowDTO selectedHistoryRow;
    private int selectedRowIndex;
    private boolean lockHistoryButton = false;

    @FXML private Label userHistoryLabel;
    @FXML private TableView<HistoryRowDTO> historyTable;
    @FXML private TableColumn<HistoryRowDTO, Number> colRunNumber;
    @FXML private TableColumn<HistoryRowDTO, String> colMainProgramOrFunction;
    @FXML private TableColumn<HistoryRowDTO, String> colProgramName;
    @FXML private TableColumn<HistoryRowDTO, String> colArchitectureType;
    @FXML private TableColumn<HistoryRowDTO, Number> colDegree;
    @FXML private TableColumn<HistoryRowDTO, Number> colCycles;
    @FXML private TableColumn<HistoryRowDTO, Number> colResult;
    @FXML private Button reRunButton;
    @FXML private Button showStatusButton;

    @FXML private ListView<String> historyListView;     // TODO: delete


    @FXML
    protected void initialize() {
        reRunButton.setDisable(true);
        showStatusButton.setDisable(true);

        colRunNumber.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(historyTable.getItems().indexOf(cellData.getValue()) + 1));
        // TODO: write the set for the reset of the columns
        colDegree.setCellValueFactory(new PropertyValueFactory<>("degree"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colCycles.setCellValueFactory(new PropertyValueFactory<>("totalCycles"));
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setProperty(StringProperty selectedUserProperty, StringProperty currentUserLoginProperty) {
        this.selectedUserProperty = selectedUserProperty;
        this.currentUserLoginProperty = currentUserLoginProperty;
    }

    public void initializeListeners() {
        selectedUserProperty.addListener((obs, oldUser, newUser) -> {
                String targetUser = (newUser == null || newUser.isEmpty())
                        ? currentUserLoginProperty.get()   // fallback: current user
                        : newUser;

                userHistoryLabel.setText("History of: " + targetUser);
                loadUserHistory(targetUser);
                });

        // Listen to row selection and notify the main controller
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newHistoryRowSelected) -> {
            if (newHistoryRowSelected == null) {    // No selected
                reRunButton.setDisable(true);
                showStatusButton.setDisable(true);
                this.selectedHistoryRow = null;
            } else if (!lockHistoryButton) {       // A row selected and buttons aren't locked
                reRunButton.setDisable(false);
                showStatusButton.setDisable(false);
                this.selectedHistoryRow = newHistoryRowSelected;
                this.selectedRowIndex = historyTable.getSelectionModel().getSelectedIndex() + 1;
            }
        });
    }

    private void loadUserHistory(String username) {
        String finalUrl = HttpUrl
                .parse(USER_HISTORY_LIST_PAGE)
                .newBuilder()
                .addQueryParameter("username", username)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        historyListView.getItems().setAll("Failed to load history: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String json = response.body().string();
                String[] historyItems = GSON_INSTANCE.fromJson(json, String[].class);
                System.out.println("DEBUG - Server returned: " + json);

                Platform.runLater(() -> {
                    if (historyItems == null || historyItems.length == 0) {
                        historyListView.getItems().clear();
                    } else {
                        historyListView.getItems().setAll(historyItems);
                    }
                });
            }
        });
    }

    @FXML
    public void onShowStatus() {
        try {
            // Load the FXML for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    HISTORY_ROW_POP_UP_LOCATION));
            Parent root = loader.load();

            // Get controller and set data
            HistoryRowPopUpController historyRowPopUpController = loader.getController();
            historyRowPopUpController.setDataToHistoryRowPopUp(selectedHistoryRow.getVariablesToValuesSorted());

            // Create new stage for popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Run " + selectedRowIndex + ": Variables State");
            popupStage.setScene(new Scene(root, 300, 300)); // width fixed, height default
            popupStage.setResizable(true); // allow user to resize
            popupStage.show();
            clearHistoryTableRowSelection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onReRun() {
        int degree = selectedHistoryRow.getDegree();
        List<Long> inputs = selectedHistoryRow.getInputsValuesOfUser();
        //mainController.prepareForNewRun(degree, inputs);  // TODO: WRITE AND WIRE
        clearHistoryTableRowSelection();
    }

    public void setHistoryButtonsDisabled(boolean disable) {
        this.lockHistoryButton = disable;
    }

    public void clearHistoryTableRowSelection() {
        historyTable.getSelectionModel().clearSelection();
    }

    public static List<HistoryRowDTO> convertToHistoryRows(List<ProgramExecutorDTO> historyPerProgram) {
        List<HistoryRowDTO> historyRows = new ArrayList<>();
        for (int i = 0; i < historyPerProgram.size(); i++) {
            ProgramExecutorDTO dto = historyPerProgram.get(i);
            historyRows.add(new HistoryRowDTO(
                    i + 1,                 // run number
                    dto.getDegree(),
                    dto.getResult(),
                    dto.getTotalCycles(),
                    dto.getVariablesToValuesSorted(),
                    dto.getInputsValuesOfUser()
            ));
        }
        return historyRows;
    }

    public void updateHistoryTableManual() {
    //    historyTable.getItems().setAll(mainController.getHistory());
    }

    public void clearHistoryTable() {
        historyTable.getItems().clear();
    }
}
