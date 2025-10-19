package components.dashboard.mainDashboard;

import com.google.gson.JsonObject;
import services.AppService;
import utils.ui.AlertUtils;
import components.dashboard.availableFunctions.AvailableFunctionsListController;
import components.dashboard.availablePrograms.AvailableProgramsListController;
import components.dashboard.chargeCredits.ChargeCreditsController;
import components.dashboard.loadFileClient.LoadFileController;
import components.dashboard.users.UsersListController;
import components.dashboard.usersHistory.UsersHistoryController;
import components.mainAppV3.MainAppController;
import utils.ui.ToastUtil;
import dto.v2.ProgramDTO;
import dto.v3.UserDTO;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.http.HttpClientUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static utils.Constants.*;


public class DashboardController implements Closeable {

    private MainAppController mainAppController;

    private AppService appService;

    private final StringProperty selectedFilePathProperty = new SimpleStringProperty();
    private final ObjectProperty<UserDTO> selectedUserProperty = new SimpleObjectProperty<>();
    private StringProperty currentUserLoginProperty;
    private LongProperty totalCreditsAmount;

    @FXML private StackPane dashboardStackPane;
    @FXML private HBox loadFile;
    @FXML private LoadFileController loadFileController;          // must: field name = fx:id + "Controller"
    @FXML private HBox chargeCredits;
    @FXML private ChargeCreditsController chargeCreditsController;
    @FXML private VBox usersList;
    @FXML private UsersListController usersListController;        // must: field name = fx:id + "Controller"
    @FXML private VBox userHistoryList;
    @FXML private UsersHistoryController userHistoryListController;  // must: field name = fx:id + "Controller"
    @FXML private VBox availableProgramsList;
    @FXML private AvailableProgramsListController availableProgramsListController;        // must: field name = fx:id + "Controller"
    @FXML private VBox availableFunctionsList;
    @FXML private AvailableFunctionsListController availableFunctionsListController;       // must: field name = fx:id + "Controller"


    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void setProgramService(AppService appService) {
        this.appService = appService;
    }

    public void setProperty(StringProperty currentUsername, LongProperty totalCreditsAmount) {
        this.totalCreditsAmount = totalCreditsAmount;
        this.currentUserLoginProperty = currentUsername;
    }

    public void setupAfterMainAppInit() {
        if (
            loadFileController != null &&
            chargeCreditsController != null &&
            usersListController != null &&
            userHistoryListController != null &&
            availableProgramsListController != null &&
            availableFunctionsListController != null
        ) {
            initLoadFileController();
            initChargeCreditsController();
            initUserListController();
            initHistoryListController();
            initProgramsListController();
            initFunctionsListController();
        }
    }

    // TODO: move this http call
    public void loadNewFile(File file, String pathStr) {
        String finalUrl = Objects.requireNonNull(HttpUrl
                        .parse(FILE_UPLOAD_PAGE))
                .newBuilder()
                .toString();

        // Build multipart request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        XML_FILE,                    // field name expected by the servlet
                        file.getName(),            // filename
                        RequestBody.create(file, MediaType.parse("application/xml"))
                )
                .build();

        HttpClientUtil.runAsync(finalUrl, requestBody, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        AlertUtils.showError("Load failed", e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    Platform.runLater(() -> {
                        // If server returned plain text (not JSON)
                        String errorMessage = (responseBody != null && !responseBody.isBlank())
                                ? responseBody.trim()
                                : "Server returned error code " + response.code();

                        AlertUtils.showError("Load failed", errorMessage);
                    });
                    return;
                }

                ProgramDTO loadedProgramDTO = GSON_INSTANCE.fromJson(responseBody, ProgramDTO.class);

                Platform.runLater(() ->  {
                    ToastUtil.showToast(
                            dashboardStackPane,
                            "XML file uploaded successfully: " + loadedProgramDTO.getProgramName(),
                            true
                    );
                    selectedFilePathProperty.set(pathStr);
                });
            }
        });
    }

    public void switchToExecution(String programSelectedName) {
        mainAppController.switchToExecution(programSelectedName);
    }

    public void startComponentRefreshing() {
        usersListController.startListRefresher();
        availableProgramsListController.startListRefresher();
        availableFunctionsListController.startListRefresher();
    }

    public void addCreditsToUser(long amountToAdd) {

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty(CREDITS_TO_CHARGE_QUERY_PARAM, amountToAdd);
        RequestBody requestBody = RequestBody.create(GSON_INSTANCE.toJson(jsonBody), MEDIA_TYPE_JSON);

        appService.addCreditsAsync(
                CHARGE_CREDITS_PATH,
                requestBody,
                updatedCredits -> Platform.runLater(() -> {
                    totalCreditsAmount.set(updatedCredits);
                    ToastUtil.showToast(mainAppController.getRootStackPane(),
                            "Credits added successfully! New total: " + updatedCredits,
                            true);
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Charge Failed", errorMsg)
                )
        );
    }

    private void initLoadFileController() {
        loadFileController.setDashboardController(this);
        loadFileController.setProperty(selectedFilePathProperty);
        loadFileController.initializeBindings();
    }

    private void initChargeCreditsController() {
        chargeCreditsController.setDashboardController(this);
        chargeCreditsController.setProperty(totalCreditsAmount);
    }

    private void initUserListController() {
        usersListController.setProperty(selectedUserProperty);
    }

    private void initHistoryListController() {
        userHistoryListController.setDashboardController(this);
        userHistoryListController.setProperty(selectedUserProperty, currentUserLoginProperty);
        userHistoryListController.initializeListeners();
        userHistoryListController.loadInitialHistory();
    }

    private void initProgramsListController() {
        availableProgramsListController.initListeners();
        availableProgramsListController.setDashboardController(this);
    }

    private void initFunctionsListController() {
        availableFunctionsListController.initListeners();
        availableFunctionsListController.setDashboardController(this);
    }

    @Override
    public void close() {
        usersListController.close();
        availableProgramsListController.close();
        availableFunctionsListController.close();
    }

    public void loadHistoryForUser(String username) {
        String finalUrl = HttpUrl
                .parse(USER_HISTORY_LIST_PAGE)
                .newBuilder()
                .addQueryParameter("username", username)
                .build()
                .toString();

        appService.fetchUserHistoryAsync(
                finalUrl,
                historyRowV3DTOList -> Platform.runLater(() -> {
                    if (historyRowV3DTOList == null || historyRowV3DTOList.isEmpty()) {
                        userHistoryListController.clearHistoryTable();
                    } else {
                        userHistoryListController.setItemsInTable(historyRowV3DTOList);
                    }
                }),
                errorMsg -> Platform.runLater(() ->
                        AlertUtils.showError("Get User History Failed", errorMsg)
                )
        );
    }

    public void loadCurrentLoginUserHistory() {
        userHistoryListController.loadInitialHistory();
    }

    // When re-run was pressed
    public void prepareForNewRun(String programName, int newDegree, List<Long> inputs, String chosenArchitecture) {
        mainAppController.prepareForNewRun(programName, newDegree, inputs, chosenArchitecture);
    }
}
