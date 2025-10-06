package components.loadFileClient;

import components.dashboard.DashboardController;
import components.mainApp.MainAppController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static components.dashboard.DashboardController.showError;

public class LoadFileController {

    private DashboardController dashboardController;
    private StringProperty selectedFilePathProperty;


    @FXML private Button loadFileButton;
    @FXML private Label pathLabel;

    @FXML private Button chargeCreditsButton;
    @FXML private TextField chargeCreditsTextField;

    private Timeline pulseAnimation;       // Animation for button

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setProperty(StringProperty selectedFilePathProperty) {
        this.selectedFilePathProperty = selectedFilePathProperty;
    }

    public void initializeBindings() {
        pathLabel.textProperty().bind(selectedFilePathProperty);
    }

    @FXML
    private void initialize() {
        initializePulseAnimationLoadButton();
    }


    @FXML
    public void onLoadFileButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(pathLabel.getScene().getWindow());
        if (file == null) return;

        // Stop animation once a file is chosen
        if (pulseAnimation != null) {
            pulseAnimation.stop();
            loadFileButton.setScaleX(1.0);
            loadFileButton.setScaleY(1.0);
        }

        try {
            // Pass the File object instead of the InputStream
            dashboardController.loadNewFile(file, file.getAbsolutePath());
        } catch (Exception e) {
            showError("Load failed", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onChargeCreditsButton() {

    }

    private void initializePulseAnimationLoadButton() {
        // Create pulse animation for Load File button
        pulseAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(loadFileButton.scaleXProperty(), 1.0),
                        new KeyValue(loadFileButton.scaleYProperty(), 1.0)
                ),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(loadFileButton.scaleXProperty(), 1.15),
                        new KeyValue(loadFileButton.scaleYProperty(), 1.15)
                ),
                new KeyFrame(Duration.millis(2000),
                        new KeyValue(loadFileButton.scaleXProperty(), 1.0),
                        new KeyValue(loadFileButton.scaleYProperty(), 1.0)
                )
        );
        pulseAnimation.setCycleCount(Animation.INDEFINITE);
        pulseAnimation.play(); // start animation immediately
    }
}
