package components.toastMessage;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ToastUtil {

    // Displays a toast message centered at the top of the given stage
    public static void showToast(Stage stage, String message) {
        Platform.runLater(() -> {
            Label toastLabel = new Label(message);
            toastLabel.setStyle("""
                    -fx-background-color: rgba(60, 60, 60, 0.9);
                    -fx-text-fill: white;
                    -fx-padding: 10px 20px;
                    -fx-font-size: 14px;
                    -fx-background-radius: 8px;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 5, 0, 0, 2);
                    """);

            Pane root = (javafx.scene.layout.Pane) stage.getScene().getRoot();
            StackPane toastContainer = new StackPane(toastLabel);
            toastContainer.setAlignment(Pos.TOP_CENTER);
            toastContainer.setMouseTransparent(true);  // allows clicks through the toast

            // Add toast to the scene
            root.getChildren().add(toastContainer);

            // Fade out animation
            FadeTransition fade = new FadeTransition(Duration.seconds(2.5), toastContainer);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.seconds(1.5)); // stays visible briefly before fading
            fade.setOnFinished(event -> root.getChildren().remove(toastContainer));
            fade.play();
        });
    }
}
