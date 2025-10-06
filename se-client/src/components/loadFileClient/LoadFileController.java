package components.loadFileClient;

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

public class LoadFileController {

    private MainAppController mainController;
    private StringProperty selectedFilePathProperty;


    @FXML private Button loadFileButton;
    @FXML private Label pathLabel;

    @FXML private Button chargeCreditsButton;
    @FXML private TextField chargeCreditsTextField;

    private Timeline pulseAnimation;       // Animation for button

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
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
    public void onFileButtonAction() {
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

//        mainController.loadNewFile(file.toPath(), pathLabel.getScene().getWindow());
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
