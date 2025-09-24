package components.loadFile;

import components.mainApp.MainAppController;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

import static components.mainApp.MainAppController.handleTaskFailure;


public class LoadFileController {

    private MainAppController mainController;
    private StringProperty selectedFilePathProperty;

    @FXML private Label pathLabel;

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
    void openFileButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(pathLabel.getScene().getWindow());
        if (file == null) return;

        mainController.loadNewFile(file.toPath(), pathLabel.getScene().getWindow());
    }

    public static Stage showProgressDialog(javafx.concurrent.Task<?> task, javafx.stage.Window owner) {
        ProgressBar progressBar = new javafx.scene.control.ProgressBar();
        progressBar.setPrefWidth(260);
        progressBar.progressProperty().bind(task.progressProperty());

        Label msg = new javafx.scene.control.Label();
        msg.textProperty().bind(task.messageProperty());

        Button cancelBtn = new javafx.scene.control.Button("Cancel");
        cancelBtn.setOnAction(e -> task.cancel());

        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(12, msg, progressBar, cancelBtn);
        root.setStyle("-fx-padding: 16;");

        Scene scene = new javafx.scene.Scene(root);
        Stage stage = new javafx.stage.Stage();
        stage.setTitle("Loading file");
        stage.initOwner(owner);
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setScene(scene);

        stage.setOnCloseRequest(e -> task.cancel());

        stage.show();
        return stage;
    }


    // Handle task failure
    public static void handleLoadTaskFailure(javafx.concurrent.Task<?> task, javafx.stage.Stage progressStage) {
        progressStage.close();
        handleTaskFailure(task, "Load failed");
    }

    public static void showEngineError(String title, String engineMsg) {
        javafx.scene.control.Alert alert =
                new Alert(Alert.AlertType.NONE, engineMsg, ButtonType.CLOSE);

        alert.setTitle(title);
        alert.setHeaderText(null);   // no header
        alert.setGraphic(null);      // no icon
        alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }
}
