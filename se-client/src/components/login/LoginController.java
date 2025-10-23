package components.login;

import components.mainAppV3.MainAppController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.http.HttpClientUtil;
import java.io.IOException;
import static utils.Constants.LOGIN_PAGE;
import static utils.Constants.USERNAME_QUERY_PARAM;


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
                .addQueryParameter(USERNAME_QUERY_PARAM, userName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (ResponseBody ignored = response.body()) {
                    if (!response.isSuccessful()) {
                        String responseBody = HttpClientUtil.readResponseBodySafely(response);
                        if (responseBody != null) {
                            String cleanedMessage = cleanErrorMessage(responseBody);
                            Platform.runLater(() -> errorMessageProperty.set(cleanedMessage));
                        }
                    } else {
                        Platform.runLater(() -> {
                            mainAppController.updateUserName(userName);
                            mainAppController.switchToDashboard();
                        });
                    }
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
    private void userNameKeyTyped() {
        errorMessageProperty.set("");
    }

    private String cleanErrorMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            return "Unknown error occurred.";
        }

        // Extract key parts from HTML, like <h1>, <p>, <b>
        StringBuilder extracted = new StringBuilder();

        java.util.regex.Matcher h1 = java.util.regex.Pattern
                .compile("<h1>(.*?)</h1>", java.util.regex.Pattern.DOTALL)
                .matcher(rawMessage);
        if (h1.find()) extracted.append(h1.group(1)).append("\n");

        java.util.regex.Matcher p = java.util.regex.Pattern
                .compile("<p>(.*?)</p>", java.util.regex.Pattern.DOTALL)
                .matcher(rawMessage);
        while (p.find()) extracted.append(p.group(1)).append("\n");

        // Fallback: if nothing matched, just strip all tags but keep text
        String text = extracted.length() > 0
                ? extracted.toString()
                : rawMessage.replaceAll("<[^>]*>", "");

        // Clean and normalize whitespace
        text = text.replaceAll("\\s+", " ").trim();

        // Optional: shorten extremely long content
        if (text.length() > 500) text = text.substring(0, 500) + "...";

        return text;
    }
}
