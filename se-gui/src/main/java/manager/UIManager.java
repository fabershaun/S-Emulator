package manager;

import components.mainApp.MainAppController;
import engine.EngineImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;


public class UIManager extends Application {
    private final static String FULL_APP_FXML_RESOURCE = "/components/mainApp/mainApp.fxml";

    public void run() {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader();

        // Load main fxml
        URL url = getClass().getResource(FULL_APP_FXML_RESOURCE);
        loader.setLocation(url);
        Parent root = loader.load();

        // Wire up controller
        EngineImpl engine = new EngineImpl();
        MainAppController mainAppController = loader.getController();
        mainAppController.setEngine(engine);

        // Set stage
        stage.setTitle("S-Emulator");
        Scene scene = new Scene(root, 1200,600);
        stage.setScene(scene);
        stage.show();
    }
}
