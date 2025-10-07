package components.dashboard.usersHistory.historyRowPopUp;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Map;

public class HistoryRowPopUpController {
    @FXML private TableView<Map.Entry<String, Long>> variablesTableView;
    @FXML private TableColumn<Map.Entry<String, Long>, String> colVariables;
    @FXML private TableColumn<Map.Entry<String, Long>, Long> colVariablesValue;

    @FXML
    private void initialize() {

        // Variable table:
        colVariables.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey()));
        colVariablesValue.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getValue()).asObject());
    }

    public void setDataToHistoryRowPopUp(Map<String, Long> variablesAndValues) {
        if (variablesAndValues != null) {
            variablesTableView.getItems().setAll(variablesAndValues.entrySet());
        } else {
            variablesTableView.getItems().clear();
        }
    }
}
