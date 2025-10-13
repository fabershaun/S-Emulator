package main;

import components.mainAppV3.MainAppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import static utils.Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION;

public class AppClient extends Application {
    private MainAppController mainAppController;

    public void run() {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader();

        // Load main fxml
        URL url = getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION);
        loader.setLocation(url);
        Parent root = loader.load();
        mainAppController = loader.getController();

        // Set stage
        stage.setTitle("S-Emulator");
        Scene scene = new Scene(root, 1200,600);
        stage.setScene(scene);
        stage.show();
    }

    // todo
//    @Override
//    public void stop() throws Exception {
//        HttpClientUtil.shutdown();
//        mainAppController.close();
//    }
}
