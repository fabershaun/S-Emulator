package components.dashboard;

import components.loadFileClient.LoadFileController;
import components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import okhttp3.*;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import static utils.Constants.FILE_UPLOAD_PATH;
import static utils.Constants.XML_FILE;


public class DashboardController {

    private MainAppController mainAppController;

    @FXML private BorderPane loadFile;
    @FXML private LoadFileController loadFileController;        // must: field name = fx:id + "Controller"

    private final StringProperty selectedFilePath = new SimpleStringProperty();


    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    @FXML
    public void initialize() {
        if (loadFileController != null) {
            loadFileController.setDashboardController(this);
            loadFileController.setProperty(selectedFilePath);
            loadFileController.initializeBindings();
        }
    }

    public void loadNewFile(File file, String pathStr) {
        String finalUrl = HttpUrl
                .parse(FILE_UPLOAD_PATH)
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
                        selectedFilePath.set(pathStr);
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
}
