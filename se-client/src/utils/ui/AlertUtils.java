package utils.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertUtils {

    // Static utility method to show an error alert safely from any thread
    public static void showError(String title, String message) {
        // Ensure this runs on the JavaFX Application Thread
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE, message, ButtonType.CLOSE);
            alert.setTitle(title);
            alert.setHeaderText(null);   // no header
            alert.setGraphic(null);      // no icon
            alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            alert.showAndWait();
        });
    }
}
