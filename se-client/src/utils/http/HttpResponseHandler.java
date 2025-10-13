package utils.http;

import utils.ui.AlertUtils;
import javafx.application.Platform;

import static utils.http.Constants.GSON_INSTANCE;

public class HttpResponseHandler {

    // Handles all common HTTP error responses
    public static void handleErrorResponse(int statusCode, String responseBody, String contextMessage) {
        try {
            String errorMessage = GSON_INSTANCE.fromJson(responseBody, String.class);

            Platform.runLater(() -> {
                switch (statusCode) {
                    case 400 -> AlertUtils.showError("Invalid Request", contextMessage + ": " + errorMessage);
                    case 401 -> AlertUtils.showError("Unauthorized", "You are not logged in or your session has expired.");
                    case 404 -> AlertUtils.showError("Not Found", contextMessage + ": " + errorMessage);
                    case 500 -> AlertUtils.showError("Server Error", errorMessage);
                    default -> AlertUtils.showError("Unexpected Error",
                            "Server returned code " + statusCode + ":\n" + errorMessage);
                }
            });
        } catch (Exception e) {
            Platform.runLater(() ->
                    AlertUtils.showError("Response Parse Failed",
                            "Server returned " + statusCode + " with invalid JSON:\n" + responseBody)
            );
        }
    }
}
