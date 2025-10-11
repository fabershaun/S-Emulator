package components.mainAppV3;

import components.dashboard.mainDashboard.DashboardController;
import components.execution.mainExecution.MainExecutionController;
import components.login.LoginController;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

import static utils.Constants.*;

public class MainAppController {

    private final LongProperty totalCreditsAmount = new SimpleLongProperty();

    @FXML private Label availableCreditsLabel;
    @FXML private Label userNameLabel;
    private final StringProperty currentUserName;
    @FXML private AnchorPane mainPanel;     // Dynamic area (login / dashboard)

    private Parent loginScreen;
    private Parent dashboardScreen;
    private Parent executionScreen;

    private DashboardController dashboardController;
    private MainExecutionController executionController;
    private LoginController loginController;


    public MainAppController() {
        this.currentUserName = new SimpleStringProperty(ANONYMOUS);
    }

    @FXML
    public void initialize() {
        userNameLabel.textProperty().bind(currentUserName);
        availableCreditsLabel.textProperty().bind(totalCreditsAmount.asString());

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
            dashboardController.setProperty(currentUserName, totalCreditsAmount);
            dashboardController.setupAfterMainAppInit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadExecutionPage(String programSelectedName) {
        URL executionPage = getClass().getResource(EXECUTION_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(executionPage);
            executionScreen = fxmlLoader.load();
            executionController = fxmlLoader.getController();
            executionController.setMainAppController(this);
            executionController.setProperty(currentUserName, totalCreditsAmount);
            executionController.setupAfterMainAppInit(programSelectedName);
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
        dashboardController.setActive();
    }

    public void switchToLogin() {
        Platform.runLater(() -> {
            currentUserName.set(ANONYMOUS);
            setMainPanelTo(loginScreen);
        });
    }

    public void switchToExecution(String programSelectedName) {
        loadExecutionPage(programSelectedName);
        setMainPanelTo(executionScreen);
    }

    public StringProperty currentUserNameProperty() {
        return currentUserName;
    }
}
