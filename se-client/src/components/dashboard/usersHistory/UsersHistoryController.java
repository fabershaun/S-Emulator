package components.dashboard.usersHistory;

import components.dashboard.mainDashboard.DashboardController;
import components.dashboard.usersHistory.historyRowPopUp.HistoryRowPopUpController;
import dto.v3.HistoryRowV3DTO;
import dto.v3.UserDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

import static components.dashboard.availablePrograms.AvailableProgramsListController.capitalizeOnlyFirstLetter;
import static utils.Constants.*;

public class UsersHistoryController {

    private DashboardController dashboardController;
    private ObjectProperty<UserDTO> selectedUserProperty;
    private StringProperty currentUserLoginProperty;

    private HistoryRowV3DTO selectedHistoryRow;
    private int selectedRowIndex;
    private final boolean lockHistoryButton = false;

    @FXML private Label userHistoryLabel;
    @FXML private TableView<HistoryRowV3DTO> historyTable;
    @FXML private TableColumn<HistoryRowV3DTO, Number> colRunNumber;
    @FXML private TableColumn<HistoryRowV3DTO, String> colMainProgramOrFunction;
    @FXML private TableColumn<HistoryRowV3DTO, String> colProgramName;
    @FXML private TableColumn<HistoryRowV3DTO, String> colArchitectureType;
    @FXML private TableColumn<HistoryRowV3DTO, Number> colDegree;
    @FXML private TableColumn<HistoryRowV3DTO, Number> colCycles;
    @FXML private TableColumn<HistoryRowV3DTO, Number> colResult;
    @FXML private Button reRunButton;
    @FXML private Button showStatusButton;


    @FXML
    protected void initialize() {
        reRunButton.setDisable(true);
        showStatusButton.setDisable(true);

        colRunNumber.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(historyTable.getItems().indexOf(cellData.getValue()) + 1));
        colMainProgramOrFunction.setCellValueFactory(new PropertyValueFactory<>("programType"));
        colProgramName.setCellValueFactory(new PropertyValueFactory<>("programUserString"));
        colArchitectureType.setCellValueFactory(new PropertyValueFactory<>("architectureChoice"));

        colDegree.setCellValueFactory(new PropertyValueFactory<>("degree"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colCycles.setCellValueFactory(new PropertyValueFactory<>("totalCycles"));

        colProgramName.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(capitalizeOnlyFirstLetter(item));
                }
            }
        });
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setProperty(ObjectProperty<UserDTO> selectedUserProperty, StringProperty currentUserLoginProperty) {
        this.selectedUserProperty = selectedUserProperty;
        this.currentUserLoginProperty = currentUserLoginProperty;
    }


    public void loadInitialHistory() {
        if (currentUserLoginProperty != null && currentUserLoginProperty.get() != null) {
            userHistoryLabel.setText("History of: " + currentUserLoginProperty.get());
            loadHistoryForUser(currentUserLoginProperty.get());
        }
    }

    public void initializeListeners() {
        selectedUserProperty.addListener((obs, oldUser, newUser) -> {

            if (newUser != null) {
                String selectedUserName = newUser.getUserName();
                userHistoryLabel.setText("History of: " + selectedUserName);
                loadHistoryForUser(selectedUserName);
            }
            else {
                String loggedUserName = currentUserLoginProperty.get();
                userHistoryLabel.setText("History of: " + loggedUserName);
                loadHistoryForUser(loggedUserName);
            }
        });

        // Listen to row selection and notify the main controller
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newHistoryRowSelected) -> {
            if (newHistoryRowSelected == null) {    // No selected
                reRunButton.setDisable(true);
                showStatusButton.setDisable(true);
                this.selectedHistoryRow = null;
            } else if (!lockHistoryButton) {       // A row selected and buttons aren't locked
                reRunButton.setDisable(false);
                showStatusButton.setDisable(false);
                this.selectedHistoryRow = newHistoryRowSelected;
                this.selectedRowIndex = historyTable.getSelectionModel().getSelectedIndex() + 1;
            }
        });
    }

    private void loadHistoryForUser(String username) {
        dashboardController.loadHistoryForUser(username);
    }

    @FXML
    public void onShowStatus() {
        try {
            // Load the FXML for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    HISTORY_ROW_POP_UP_LOCATION));
            Parent root = loader.load();

            // Get controller and set data
            HistoryRowPopUpController historyRowPopUpController = loader.getController();
            historyRowPopUpController.setDataToHistoryRowPopUp(selectedHistoryRow.getVariablesToValuesSorted());

            // Create new stage for popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Run " + selectedRowIndex + ": Variables State");
            popupStage.setScene(new Scene(root, 300, 300)); // width fixed, height default
            popupStage.setResizable(true); // allow user to resize
            popupStage.show();
            clearHistoryTableSelection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onReRun() {
        String programName = selectedHistoryRow.getProgramName();
        int degree = selectedHistoryRow.getDegree();
        List<Long> inputs = selectedHistoryRow.getInputsValuesOfUser();
        String chosenArchitecture = selectedHistoryRow.getArchitectureChoice();
        dashboardController.prepareForNewRun(programName, degree, inputs, chosenArchitecture);
    }

    public void clearHistoryTableSelection() {
        historyTable.getSelectionModel().clearSelection();
    }

    public void clearHistoryTable() {
        historyTable.getItems().clear();
    }

    public void setItemsInTable(List<HistoryRowV3DTO> historyRowV3DTOList) {
        historyTable.getItems().setAll(historyRowV3DTOList);
    }
}
