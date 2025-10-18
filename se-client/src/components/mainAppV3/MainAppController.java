package components.mainAppV3;

import components.dashboard.mainDashboard.DashboardController;
import components.execution.mainExecution.MainExecutionController;
import components.login.LoginController;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import services.ProgramPollingService;
import services.AppService;
import utils.ui.AlertUtils;
import utils.ui.ToastUtil;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static utils.Constants.*;

public class MainAppController {

    private final ProgramPollingService programPollingService = new ProgramPollingService();
    private AppService appService = new AppService();

    private final LongProperty totalCreditsAmount = new SimpleLongProperty();
    private final StringProperty currentUserNameLogin;
    private Parent loginScreen;
    private Parent dashboardScreen;
    private Parent executionScreen;

    private DashboardController dashboardController;
    private MainExecutionController executionController;
    private LoginController loginController;

    @FXML private StackPane rootStackPane;
    @FXML private Label availableCreditsLabel;
    @FXML private Label userNameLabel;
    @FXML private AnchorPane mainPanel;     // Dynamic area (login / dashboard)
    @FXML private GridPane headerGridPane;
    private Button backToDashboardButton;

    public MainAppController() {
        this.currentUserNameLogin = new SimpleStringProperty(ANONYMOUS);
    }

    @FXML
    public void initialize() {
        userNameLabel.textProperty().bind(currentUserNameLogin);
        availableCreditsLabel.textProperty().bind(totalCreditsAmount.asString());

        // Build the back button dynamically
        createBackToDashboardButton();

        // Prepare component
        loadLoginPage();
        setMainPanelTo(loginScreen);
    }

    @FXML
    private void onBackToDashboardClicked() {
        programPollingService.stopPolling();
        switchToDashboard();
    }

    private void loadDashboardPage() {
        URL dashboardPage = getClass().getResource(DASHBOARD_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(dashboardPage);
            dashboardScreen = fxmlLoader.load();
            dashboardController = fxmlLoader.getController();
            dashboardController.setMainAppController(this);
            dashboardController.setProgramService(appService);
            dashboardController.setProperty(currentUserNameLogin, totalCreditsAmount);
            dashboardController.setupAfterMainAppInit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadExecutionPageInternal(String programName, Runnable onProgramLoaded) {
        URL executionPage = getClass().getResource(EXECUTION_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(executionPage);
            executionScreen = fxmlLoader.load();
            executionController = fxmlLoader.getController();

            // Set shared properties
            executionController.setMainAppController(this);
            executionController.setProgramService(appService);
            executionController.setProgramPollingService(programPollingService);
            executionController.setProperty(currentUserNameLogin, totalCreditsAmount);

            // Load program data
            executionController.setupAfterMainAppInit(programName, onProgramLoaded);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (backToDashboardButton != null) {
            backToDashboardButton.setVisible(true);
            backToDashboardButton.setManaged(true);
        }

        setMainPanelTo(executionScreen);
    }

    private void loadExecutionPage(String programSelectedName) {
        loadExecutionPageInternal(programSelectedName, null);
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

    public void prepareForNewRun(String programName, int newDegree, List<Long> inputs, String chosenArchitecture) {
        loadExecutionPageInternal(programName, () -> {
            executionController.prepareForNewRun(newDegree, inputs, chosenArchitecture);
        });
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
        this.currentUserNameLogin.set(userName);
    }

    public void switchToDashboard() {
        if (dashboardScreen == null) {
            loadDashboardPage();
        }

        setMainPanelTo(dashboardScreen);
        if (backToDashboardButton != null) {
            backToDashboardButton.setVisible(false);
            backToDashboardButton.setManaged(false);
        }
        dashboardController.startComponentRefreshing();
        dashboardController.loadCurrentLoginUserHistory();

        // updates user's credits:
        appService.fetchUserCreditsAsync(
                FETCH_CREDITS_PATH,
                null,
                credits -> Platform.runLater(() -> totalCreditsAmount.set(credits)),
                errorMsg -> Platform.runLater(() -> AlertUtils.showError("Error", errorMsg))
        );
    }

    public void switchToLogin() {
        Platform.runLater(() -> {
            currentUserNameLogin.set(ANONYMOUS);
            setMainPanelTo(loginScreen);
        });
    }

    public void switchToExecution(String programSelectedName) {
        loadExecutionPage(programSelectedName);
        setMainPanelTo(executionScreen);
    }

    private void createBackToDashboardButton() {
        // Create button instance
        Button backButton = new Button("← Back to Dashboard");
        backButton.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-background-color: #f0f0f0; " +
                        "-fx-border-color: #ccc; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;"
        );

        // Define click action
        backButton.setOnAction(event -> switchToDashboard());

        // Hidden by default (login screen)
        backButton.setVisible(false);

        // Optionally add tooltip
        Tooltip tooltip = new Tooltip("Return to Dashboard");
        Tooltip.install(backButton, tooltip);

        // Find the left HBox in the header and insert the button
        for (javafx.scene.Node node : headerGridPane.getChildren()) {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);

            if (col != null && row != null && col == 1 && row == 1 && node instanceof HBox hbox) {
                hbox.getChildren().add(0, backButton); // insert at the start
                this.backToDashboardButton = backButton; // save reference
                break;
            }
        }
    }

    public StackPane getRootStackPane() {
        return rootStackPane;
    }

    public void popUpToastMessage(String message, boolean isSuccess) {
        ToastUtil.showToast(
                rootStackPane,
                message,
                isSuccess
        );
    }
}
