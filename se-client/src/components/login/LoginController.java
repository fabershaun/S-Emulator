package components.login;

import components.mainApp.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import utils.HttpClientUtil;
import java.io.IOException;
import static utils.Constants.LOGIN_PAGE;


public class LoginController {

    @FXML private TextField userNameTextField;
    @FXML public Label errorMessageLabel;

    private MainAppController mainAppController;
    private final StringProperty errorMessageProperty = new SimpleStringProperty();

    public void setMainAppController(MainAppController mainAppController) {
        this.mainAppController = mainAppController;
    }

    @FXML
    private void initialize() {
        errorMessageLabel.textProperty().bind(errorMessageProperty);
    }

    @FXML
    private void onLoginButtonClicked() {
        String userName = userNameTextField.getText();
        if (userName.isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        String finalUrl = HttpUrl
                .parse(LOGIN_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() ->
                            errorMessageProperty.set("Something went wrong: " + responseBody)
                    );
                } else {
                    Platform.runLater(() -> {
                        mainAppController.updateUserName(userName);
                        mainAppController.switchToDashboard();
                    });
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }
        });
    }

    @FXML
    private void onQuitButtonClicked() {
        Platform.exit();
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }
}
