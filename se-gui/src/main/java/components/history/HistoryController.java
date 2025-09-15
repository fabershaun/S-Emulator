package components.history;

import components.mainApp.MainAppController;
import dto.ProgramExecutorDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;


public class HistoryController {

    private MainAppController mainController;
    private ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty;
    private StringProperty programOrFunctionProperty;

    @FXML private TableView<ProgramExecutorDTO> historyTable;
    @FXML private TableColumn<ProgramExecutorDTO, Number> colCycles;
    @FXML private TableColumn<ProgramExecutorDTO, Number> colDegree;
    @FXML private TableColumn<ProgramExecutorDTO, Number> colResult;
    @FXML private TableColumn<ProgramExecutorDTO, Number> colRunNumber;
    @FXML private Button reRunButton;
    @FXML private Button showStatusButton;

    @FXML
    protected void initialize() {
        colRunNumber.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(historyTable.getItems().indexOf(cellData.getValue()) + 1));
        colDegree.setCellValueFactory(new PropertyValueFactory<>("degree"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colCycles.setCellValueFactory(new PropertyValueFactory<>("totalCycles"));
    }

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setProperty(ObjectProperty<ProgramExecutorDTO> programAfterExecuteProperty, StringProperty programOrFunctionProperty) {
        this.programAfterExecuteProperty = programAfterExecuteProperty;
        this.programOrFunctionProperty = programOrFunctionProperty;
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

        programOrFunctionProperty.addListener((obs, oldProgName, newProgramSelected) -> {
            if (newProgramSelected != null) {
                List<ProgramExecutorDTO> historyPerProgram = mainController.getHistory();
                historyTable.getItems().setAll(historyPerProgram);
            } else {
                historyTable.getItems().clear();
            }
        });
    }

}
