package components.history;

import components.history.historyRowPopUp.HistoryRowPopUpController;
import components.mainApp.MainAppController;
import dto.ProgramExecutorDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;


public class HistoryController {

    private MainAppController mainController;
    private ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty;

    private ProgramExecutorDTO selectedHistoryRow;
    private int selectedRowIndex;

    @FXML private TableView<ProgramExecutorDTO> historyTable;
    @FXML private TableColumn<ProgramExecutorDTO, Number> colCycles;
    @FXML private TableColumn<ProgramExecutorDTO, Number> colDegree;
    @FXML private TableColumn<ProgramExecutorDTO, Number> colResult;
    @FXML private TableColumn<ProgramExecutorDTO, Number> colRunNumber;
    @FXML private Button reRunButton;
    @FXML private Button showStatusButton;


    @FXML
    protected void initialize() {
        reRunButton.setDisable(true);
        showStatusButton.setDisable(true);

        colRunNumber.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(historyTable.getItems().indexOf(cellData.getValue()) + 1));
        colDegree.setCellValueFactory(new PropertyValueFactory<>("degree"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colCycles.setCellValueFactory(new PropertyValueFactory<>("totalCycles"));
    }

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setProperty(ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty) {
        this.programAfterExecuteProperty = programAfterExecuteProperty;
    }

    public void initializeListeners() {
        // After new run -> update history table
        programAfterExecuteProperty.addListener((obs, oldProgEx, newProgramExecutorDTO) -> {
            if (newProgramExecutorDTO != null) {
                List<ProgramExecutorDTO> historyPerProgram = mainController.getHistory();
                historyTable.getItems().setAll(historyPerProgram);
            } else {
                historyTable.getItems().clear();
            }
        });

        // After program select -> update history table
//        programSelectorProperty.addListener((obs, oldProgName, newProgramSelected) -> {
//            if (newProgramSelected != null) {
//                List<ProgramExecutorDTO> historyPerProgram = mainController.getHistory();
//                historyTable.getItems().setAll(historyPerProgram);
//            } else {
//                historyTable.getItems().clear();
//            }
//        });

        // Listen to row selection and notify the main controller
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newHistoryRowSelected) -> {
            if (newHistoryRowSelected == null) {    // No selected
                reRunButton.setDisable(true);
                showStatusButton.setDisable(true);
                this.selectedHistoryRow = null;
            } else {       // A row selected
                reRunButton.setDisable(false);
                showStatusButton.setDisable(false);
                this.selectedHistoryRow = newHistoryRowSelected;
                this.selectedRowIndex = historyTable.getSelectionModel().getSelectedIndex() + 1;
            }
        });
    }

    public void fillHistoryTable(List<ProgramExecutorDTO> historyOfProgramList) {
        if(historyOfProgramList.isEmpty()) {
            historyTable.getItems().clear();
        } else {
            historyTable.getItems().setAll(historyOfProgramList);
        }

    }

    public void onShowStatus() {
        try {
            // Load the FXML for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/components/history/historyRowPopUp/historyRowPopUp.fxml"));
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onReRun() {
        int degree = selectedHistoryRow.getDegree();
        List<Long> inputs = selectedHistoryRow.getInputsValuesOfUser();
        mainController.prepareForNewRun(degree, inputs);
    }

    public void clearHistory() { historyTable.getItems().clear(); }
}
