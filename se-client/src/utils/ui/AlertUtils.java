package utils.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;

public class AlertUtils {

    // Static utility method to show an error alert safely from any thread
    public static void showError(String title, String message) {
        // Ensure this runs on the JavaFX Application Thread
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE, message, ButtonType.CLOSE);
            alert.setTitle(title);
            alert.setHeaderText(null);   // no header
            alert.setGraphic(null);      // no icon

            // Configure label to display multiline text properly
            Label label = new Label(message);
            label.setWrapText(true);                      // Enable line wrapping
            label.setTextAlignment(TextAlignment.LEFT);   // Align text to the left
            label.setMaxWidth(400);                       // Limit width for readability
            label.setPrefWidth(400);                      // Preferred width for wrapping
            label.setMinHeight(Region.USE_PREF_SIZE);     // Auto-adjust height to content

            // Set the label as the content of the alert
            alert.getDialogPane().setContent(label);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.getDialogPane().setPrefWidth(400);      // Control overall alert width

            alert.showAndWait();
        });
    }
}
