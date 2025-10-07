package components.dashboard.mainDashboard;

import components.dashboard.availablePrograms.AvailableProgramsController;
import components.dashboard.loadFileClient.LoadFileController;
import components.dashboard.users.UsersListController;
import components.dashboard.usersHistory.UsersHistoryController;
import components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import static utils.Constants.FILE_UPLOAD_PAGE;
import static utils.Constants.XML_FILE;


public class DashboardController implements Closeable {

    private MainAppController mainAppController;
    private String currentUsername;

    @FXML private BorderPane loadFile;
    @FXML private LoadFileController loadFileController;          // must: field name = fx:id + "Controller"
    @FXML private VBox usersList;
    @FXML private UsersListController usersListController;        // must: field name = fx:id + "Controller"
    @FXML private VBox userHistoryList;
    @FXML private UsersHistoryController userHistoryListController;  // must: field name = fx:id + "Controller"
    @FXML private VBox availableProgramsList;
    @FXML private AvailableProgramsController availableProgramsListController;        // must: field name = fx:id + "Controller"
    @FXML private VBox availableFunctionsList;
    @FXML private AvailableProgramsController availableFunctionsListController;       // must: field name = fx:id + "Controller"
    private final StringProperty selectedFilePathProperty = new SimpleStringProperty();


    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize() {
        if (
            loadFileController != null &&
            usersListController != null &&
            userHistoryListController != null &&
            availableProgramsListController != null &&
            availableFunctionsListController != null
        ) {
            initLoadController();
            intiUserListController();
            initHistoryListController();
        }
    }

    private void initLoadController() {
        loadFileController.setDashboardController(this);
        loadFileController.setProperty(selectedFilePathProperty);
        loadFileController.initializeBindings();
    }

    private void intiUserListController() {

    }

    private void initHistoryListController() {
        userHistoryListController.setDashboardController(this);
        userHistoryListController.setProperty(this.selectedUsernameProperty());
        userHistoryListController.initializeListeners();
        userHistoryListController.initializeDefaultHistory();
    }

    public void setActive() {
        usersListController.startListRefresher();
    }

    public void loadNewFile(File file, String pathStr) {
        String finalUrl = HttpUrl
                .parse(FILE_UPLOAD_PAGE)
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
                        showError("Load failed", e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() ->
                            showError("Load failed", responseBody)
                    );
                } else {
                    Platform.runLater(() -> {
                        selectedFilePathProperty.set(pathStr);
                    });
                }
            }
        });
    }

    // Handle request failure
    public static void showError(String title, String message) {
        javafx.scene.control.Alert alert =
                new Alert(Alert.AlertType.NONE, message, ButtonType.CLOSE);

        alert.setTitle(title);
        alert.setHeaderText(null);   // no header
        alert.setGraphic(null);      // no icon
        alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    @Override
    public void close() throws IOException {

    }

    public String getLoginUserName() {
        return mainAppController.getLoginUsername();
    }

    public StringProperty selectedUsernameProperty() {
        return usersListController.selectedUserPropertyProperty();
    }
}
