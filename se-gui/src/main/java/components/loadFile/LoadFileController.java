package components.loadFile;

import components.mainApp.MainAppController;
import dto.ProgramDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;


public class LoadFileController {

    private MainAppController mainController;
    @FXML private Label pathLabel;

    private StringProperty selectedFilePathProperty;
    private ObjectProperty<ProgramDTO> currentProgramProperty;

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setProperty(StringProperty selectedFilePathProperty, ObjectProperty<ProgramDTO> currentProgramProperty) {
        this.selectedFilePathProperty = selectedFilePathProperty;
        this.currentProgramProperty = currentProgramProperty;
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

        javafx.concurrent.Task<Void> task = createLoadFileTask(file);
        // TODO: להוסיף בדיקות במנוע - לבדוק אם אין הפניה לפונקציה שאני מוגדרת במסגרת הקובץ / הפונקציות שבקובץ

        Stage progressStage = showProgressDialog(task, pathLabel.getScene().getWindow());

        // Run by JAT (javafx thread)
        task.setOnSucceeded(ev -> handleTaskSuccess(file, progressStage));
        task.setOnFailed(ev -> handleTaskFailure(task, progressStage));
        task.setOnCancelled(ev -> progressStage.close());

        new Thread(task, "load-xml-task").start();
    }

    private Stage showProgressDialog(javafx.concurrent.Task<?> task, javafx.stage.Window owner) {
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

    // Helper method to create the loading task
    private Task<Void> createLoadFileTask(File file) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Preparing");
                updateProgress(0, 100);

                // Simulate short progress before actual load
                for (int p = 0; p <= 40; p += 10) {
                    if (isCancelled()) return null;
                    Thread.sleep(150);
                    updateProgress(p, 100);
                }

                updateMessage("Loading");
                try {
                    mainController.loadNewFile(file.toPath());
                } catch (exceptions.EngineLoadException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // Simulate remaining progress after loading
                for (int p = 50; p <= 100; p += 10) {
                    if (isCancelled()) return null;
                    Thread.sleep(120);
                    updateProgress(p, 100);
                }

                updateMessage("Done");
                return null;
            }
        };
    }

    // Handle task success
    private void handleTaskSuccess(File file, javafx.stage.Stage progressStage) {
        progressStage.close();

        //state.isFileSelectedProperty().set(true);
        selectedFilePathProperty.set(file.getAbsolutePath());
        currentProgramProperty.set(mainController.getCurrentProgram());
    }

    // Handle task failure
    private void handleTaskFailure(javafx.concurrent.Task<?> task, javafx.stage.Stage progressStage) {
        progressStage.close();
        Throwable ex = task.getException();
        String msg;

        if (ex instanceof exceptions.EngineLoadException) {
            msg = ex.getMessage();
        } else if (ex != null && ex.getCause() instanceof exceptions.EngineLoadException) {
            msg = ex.getCause().getMessage();
        } else {
            msg = "Unexpected error";
        }

        showEngineError(msg);
        //state.isFileSelectedProperty().set(false);
    }

    private void showEngineError(String engineMsg) {
        javafx.scene.control.Alert alert =
                new Alert(Alert.AlertType.NONE, engineMsg, ButtonType.CLOSE);

        alert.setTitle("Load failed");
        alert.setHeaderText(null);   // no header
        alert.setGraphic(null);      // no icon
        alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }
}
