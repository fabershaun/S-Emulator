package magager;

import engine.Engine;
import engine.EngineImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;


public class UIManager extends Application {
    private final static String FULL_APP_FXML_RESOURCE = "/subComponents/fullApp/fullApp.fxml";

    public void run() {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(FULL_APP_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        Parent root = fxmlLoader.load(url.openStream());

        Scene scene = new Scene(root, 1000,550);
        stage.setTitle("S-Emulator GUI");
        stage.setScene(scene);
        stage.show();
    }
}
