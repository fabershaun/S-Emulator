package subComponents.loadFile;

import exceptions.EngineLoadException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import subComponents.fullApp.FullAppController;
import java.io.File;


public class LoadFileController {

    private FullAppController mainController;
    @FXML private Button loadFileButton;
    @FXML private TextField pathTextField;

    public void setMainController(FullAppController mainController) {
        this.mainController = mainController;
    }

    @FXML
    void onLoadFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select XML File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fc.showOpenDialog(pathTextField.getScene().getWindow());
        if (file == null) {
            return;
        }

        loadFileButton.setDisable(true);

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
            loadFileButton.setDisable(false);
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
