package components.mainInstructionsTable;

import components.mainApp.MainAppController;
import components.topToolBar.HighlightSelectionModel;
import dto.InstructionDTO;
import dto.ProgramDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Controller for the main instructions table.
 * Handles program updates and row selection behavior.
 */
public class MainInstructionsTableController {

    private MainAppController mainController;
    private HighlightSelectionModel highlightModel;
    private ObjectProperty<ProgramDTO> currentProgramProperty;

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colInstruction;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;

    @FXML
    protected void initialize() {
        colIndex.setCellValueFactory(new PropertyValueFactory<>("instructionNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("instructionTypeStr"));
        colLabel.setCellValueFactory(new PropertyValueFactory<>("labelStr"));
        colInstruction.setCellValueFactory(new PropertyValueFactory<>("command"));
        colCycles.setCellValueFactory(new PropertyValueFactory<>("cyclesNumber"));

        // Load the CSS file
        String cssPath = getClass().getResource("/components/mainInstructionsTable/mainInstructions.css").toExternalForm();
        instructionsTable.getStylesheets().add(cssPath);
    }

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setProperty(ObjectProperty<ProgramDTO> programProperty) {
        this.currentProgramProperty = programProperty;
    }

    public void setModels(HighlightSelectionModel model) {
        this.highlightModel = model;
        installMainTableHighlighting();       // configure cell factories
    }

    public void initializeListeners() {
        // Listen to program changes and repopulate the table
        currentProgramProperty.addListener((obs, oldProg, newProgram) -> {
            if (newProgram != null) {
                instructionsTable.getItems().setAll(newProgram.getInstructions().getProgramInstructionsDtoList());
            } else {
                instructionsTable.getItems().clear();
            }
        });

        // Listen to row selection and notify the main controller
        instructionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem == null) {
                if (mainController != null) {
                    mainController.onInstructionDeselected();
                }
            } else {
                if (mainController != null) {
                    mainController.onInstructionSelected(newItem);
                }
            }
        });
    }

    // Highlighting lines - only on the main table
    private void installMainTableHighlighting() {
        if (highlightModel == null) return;

        // Refresh table when selection changes
        highlightModel.selectedHighlightProperty().addListener((obs, oldVal, newVal) -> instructionsTable.refresh());

        instructionsTable.setRowFactory(tv -> new TableRow<InstructionDTO>() {
            @Override
            protected void updateItem(InstructionDTO item, boolean empty) {
                super.updateItem(item, empty);

                // Always clear previous style classes
                getStyleClass().remove("highlighted-row");

                if (empty || item == null) {
                    return;
                }

                String selected = highlightModel.selectedHighlightProperty().get();
                if (selected != null && !selected.isEmpty()) {
                    String regex = "\\b" + Pattern.quote(selected) + "\\b";

                    // Check against label or instruction text
                    boolean matchLabel = item.getLabelStr() != null && item.getLabelStr().matches(".*" + regex + ".*");
                    boolean matchInstruction = item.getCommand() != null && item.getCommand().matches(".*" + regex + ".*");

                    if (matchLabel || matchInstruction) {
                        getStyleClass().add("highlighted-row");
                    }
                }
            }
        });
    }
}
