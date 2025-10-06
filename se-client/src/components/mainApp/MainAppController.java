package components.mainApp;

import components.dashboard.DashboardController;
import components.login.LoginController;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;

import static utils.Constants.*;

public class MainAppController {

    @FXML private Label availableCreditsLabel;
    @FXML private Label userNameLabel;
    private final StringProperty currentUserName;
    @FXML private AnchorPane mainPanel;     // Dynamic area (login / dashboard)

    private Parent dashboardScreen;
    private Parent loginScreen;

    private DashboardController dashboardController;
    private LoginController loginController;


    public MainAppController() {
        this.currentUserName = new SimpleStringProperty(ANONYMOUS);
    }

    @FXML
    public void initialize() {
        userNameLabel.textProperty().bind(currentUserName);

        // Prepare components
        loadLoginPage();
        loadDashboardPage();

        setMainPanelTo(loginScreen);
    }

    private void loadDashboardPage() {
        URL dashboardPage = getClass().getResource(DASHBOARD_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(dashboardPage);
            dashboardScreen = fxmlLoader.load();
            dashboardController = fxmlLoader.getController();
            dashboardController.setMainAppController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLoginPage() {
        URL loginPageUrl= getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginScreen = fxmlLoader.load();
            loginController = fxmlLoader.getController();
            loginController.setMainAppController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    public void updateUserName(String userName) {
        this.currentUserName.set(userName);
    }

    public void switchToDashboard() {
        setMainPanelTo(dashboardScreen);
    }

    public void switchToLogin() {
        Platform.runLater(() -> {
            currentUserName.set(ANONYMOUS);
            setMainPanelTo(loginScreen);
        });
    }
}
