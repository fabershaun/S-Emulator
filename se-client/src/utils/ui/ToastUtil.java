package utils.ui;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ToastUtil {

    // Displays a toast message centered at the top of the given stage
    public static void showToast(StackPane rootStackPane, String message, boolean isSuccess) {
        Platform.runLater(() -> {
            Label toastLabel = new Label(message);

            // Choose color based on success/failure
            String backgroundColor = isSuccess
                    ? "rgba(50, 200, 50, 0.9)"    // soft green for success
                    : "rgba(220, 50, 50, 0.9)";   // soft red for error

            toastLabel.setStyle("""
                    -fx-background-color: %s;
                    -fx-text-fill: white;
                    -fx-padding: 10px 20px;
                    -fx-font-size: 14px;
                    -fx-background-radius: 8px;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 5, 0, 0, 2);
                    """.formatted(backgroundColor));

            StackPane overlayPane = getOrCreateOverlay(rootStackPane);

            StackPane toastContainer = new StackPane(toastLabel);
            toastContainer.setAlignment(Pos.TOP_CENTER);
            toastContainer.setPadding(new Insets(5, 0, 0, 0));
            toastContainer.setMouseTransparent(true);

            overlayPane.getChildren().add(toastContainer);

            FadeTransition fade = new FadeTransition(Duration.seconds(2.5), toastContainer);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.seconds(3.0));
            fade.setOnFinished(event -> overlayPane.getChildren().remove(toastContainer));
            fade.play();
        });
    }

    // Creates (or returns if already exists) a top overlay pane that ignores layout constraints
    private static StackPane getOrCreateOverlay(StackPane root) {
        // Search if overlay already exists
        for (Node node : root.getChildren()) {
            if (node instanceof StackPane && "toast-overlay".equals(node.getId())) {
                return (StackPane) node;
            }
        }

        // Create a new overlay StackPane if not found
        StackPane overlay = new StackPane();
        overlay.setId("toast-overlay");
        overlay.setPickOnBounds(false); // does not block mouse events
        overlay.setAlignment(Pos.TOP_CENTER);

        // Add it as the topmost layer
        root.getChildren().add(overlay);
        return overlay;
    }
}
