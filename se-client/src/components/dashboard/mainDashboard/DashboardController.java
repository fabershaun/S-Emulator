package components.dashboard.mainDashboard;

import components.dashboard.availablePrograms.AvailableProgramsListController;
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
import java.util.Objects;

import static utils.Constants.FILE_UPLOAD_PAGE;
import static utils.Constants.XML_FILE;


public class DashboardController implements Closeable {

    private MainAppController mainAppController;
    private final StringProperty selectedFilePathProperty = new SimpleStringProperty();
    private StringProperty currentUsername;

    @FXML private BorderPane loadFile;
    @FXML private LoadFileController loadFileController;          // must: field name = fx:id + "Controller"
    @FXML private VBox usersList;
    @FXML private UsersListController usersListController;        // must: field name = fx:id + "Controller"
    @FXML private VBox userHistoryList;
    @FXML private UsersHistoryController userHistoryListController;  // must: field name = fx:id + "Controller"
    @FXML private VBox availableProgramsList;
    @FXML private AvailableProgramsListController availableProgramsListController;        // must: field name = fx:id + "Controller"
    @FXML private VBox availableFunctionsList;
    @FXML private AvailableProgramsListController availableFunctionsListController;       // must: field name = fx:id + "Controller"


    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    public void setProperty(StringProperty currentUsername) {
        this.currentUsername = currentUsername;
    }

    public void setupAfterMainAppInit() {
        if (
            loadFileController != null &&
            usersListController != null &&
            userHistoryListController != null &&
            availableProgramsListController != null &&
            availableFunctionsListController != null
        ) {
            initLoadFileController();
            intiUserListController();
            initHistoryListController();
            initProgramListController();
        }
    }

    private void initLoadFileController() {
        loadFileController.setDashboardController(this);
        loadFileController.setProperty(selectedFilePathProperty);
        loadFileController.initializeBindings();
    }

    private void intiUserListController() {

    }   //TODO: WRITE

    private void initHistoryListController() {
        userHistoryListController.setDashboardController(this);
        userHistoryListController.setProperty(this.selectedUsernameProperty(), currentUsername);
        userHistoryListController.initializeListeners();
    }

    private void initProgramListController() {
        availableProgramsListController.initListeners();
    }

    public void setActive() {
        usersListController.startListRefresher();
        availableProgramsListController.startListRefresher();
    }

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
                    Platform.runLater(() -> selectedFilePathProperty.set(pathStr));
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
    public void close() {
        usersListController.close();
        availableProgramsListController.close();
        availableFunctionsListController.close();
    }

    public StringProperty selectedUsernameProperty() {
        return usersListController.selectedUserProperty();
    }
}
