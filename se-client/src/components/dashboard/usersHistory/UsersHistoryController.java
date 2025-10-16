package components.dashboard.usersHistory;

import com.google.gson.reflect.TypeToken;
import utils.ui.AlertUtils;
import components.dashboard.mainDashboard.DashboardController;
import components.dashboard.usersHistory.historyRowPopUp.HistoryRowPopUpController;
import dto.v3.HistoryRowV3DTO;
import dto.v3.UserDTO;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
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
import utils.http.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static utils.Constants.*;

public class UsersHistoryController {

    private DashboardController dashboardController;
    private ObjectProperty<UserDTO> selectedUserProperty;
    private StringProperty currentUsername;

    private HistoryRowV3DTO selectedHistoryRow;
    private int selectedRowIndex;
    private boolean lockHistoryButton = false;

    @FXML private Label userHistoryLabel;
    @FXML private TableView<HistoryRowV3DTO> historyTable;
    @FXML private TableColumn<HistoryRowV3DTO, Number> colRunNumber;
    @FXML private TableColumn<HistoryRowV3DTO, String> colMainProgramOrFunction;
    @FXML private TableColumn<HistoryRowV3DTO, String> colProgramName;
    @FXML private TableColumn<HistoryRowV3DTO, String> colArchitectureType;
    @FXML private TableColumn<HistoryRowV3DTO, Number> colDegree;
    @FXML private TableColumn<HistoryRowV3DTO, Number> colCycles;
    @FXML private TableColumn<HistoryRowV3DTO, Number> colResult;
    @FXML private Button reRunButton;
    @FXML private Button showStatusButton;


    @FXML
    protected void initialize() {
        reRunButton.setDisable(true);
        showStatusButton.setDisable(true);

        colRunNumber.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(historyTable.getItems().indexOf(cellData.getValue()) + 1));
        colMainProgramOrFunction.setCellValueFactory(new PropertyValueFactory<>("programType"));
        colProgramName.setCellValueFactory(new PropertyValueFactory<>("programUserString"));
        colArchitectureType.setCellValueFactory(new PropertyValueFactory<>("architectureChoice"));

        colDegree.setCellValueFactory(new PropertyValueFactory<>("degree"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colCycles.setCellValueFactory(new PropertyValueFactory<>("totalCycles"));
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setProperty(ObjectProperty<UserDTO> selectedUserProperty, StringProperty currentUserLoginProperty) {
        this.selectedUserProperty = selectedUserProperty;
        this.currentUsername = currentUserLoginProperty;
    }

    public void loadInitialHistory() {
        if (currentUsername != null && currentUsername.get() != null) {
            userHistoryLabel.setText("History of: " + currentUsername.get());
            loadHistoryForUser(currentUsername.get());
        } else {
            userHistoryLabel.setText("History: (no user)");
            historyTable.getItems().clear();
        }
    }

    public void initializeListeners() {
        selectedUserProperty.addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                userHistoryLabel.setText("History of: " + newUser.getUserName());
                loadHistoryForUser(newUser.getUserName());
            } else {
                loadHistoryForUser(currentUsername.get());
                userHistoryLabel.setText("History of: " + currentUsername.get());
            }
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

    private void loadHistoryForUser(String username) {
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
                        AlertUtils.showError("Server Error", "Failed to load history" + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String jsonResponse  = response.body().string();

                // Define the generic list type for deserialization
                Type listType = new TypeToken<List<HistoryRowV3DTO>>() {}.getType();

                List<HistoryRowV3DTO> historyRowV3DTOList = GSON_INSTANCE.fromJson(jsonResponse, listType);

                Platform.runLater(() -> {
                    if (historyRowV3DTOList == null || historyRowV3DTOList.isEmpty()) {
                        historyTable.getItems().clear();
                    } else {
                        historyTable.getItems().setAll(historyRowV3DTOList);
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
        throw new IllegalStateException("Need to write this code in UserHistoryController");
    }

    public void clearHistoryTableRowSelection() {
        historyTable.getSelectionModel().clearSelection();
    }

}
