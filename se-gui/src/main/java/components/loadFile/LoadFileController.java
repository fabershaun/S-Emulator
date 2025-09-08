package components.loadFile;

import components.mainApp.AppState;
import components.mainApp.MainAppController;
import exceptions.EngineLoadException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;


public class LoadFileController {

    private MainAppController mainController;
    private AppState state;
    @FXML private Button loadFileButton;
    @FXML private TextField pathTextField;

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setState(AppState state) {
        this.state = state;
    }

    @FXML
    void onLoadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(pathTextField.getScene().getWindow());
        if (file == null) {
            return;
        }

        loadFileButton.setDisable(true); // TODO: change to property

        try {
            mainController.loadNewFile(file.toPath());
            showPathOk(file.getAbsolutePath());
        }
        catch (EngineLoadException e) {
            showPathError("The file is not supported");
        }
        catch (Exception e) {
            showPathError("Unexpected error");
        }
        finally {
            loadFileButton.setDisable(false);        loadFileButton.setDisable(true); // TODO: change to property
        }
    }

    private void showPathError(String message) {
        pathTextField.setText(message);
        pathTextField.setStyle("-fx-text-inner-color: red;");
    }

    private void showPathOk(String path) {
        pathTextField.setText(path);
        pathTextField.setStyle("");         // Reset to default system color
    }

}
