package components.mainInstructionsTable;

import components.mainApp.MainAppController;
import components.topToolBar.HighlightSelectionModel;
import components.topToolBar.ProgramSelectorModel;
import dto.v2.InstructionDTO;
import dto.v2.InstructionsDTO;
import dto.v2.ProgramDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.regex.Pattern;

import static components.debuggerExecutionMenu.DebuggerExecutionMenuController.scrollToCenter;

public class MainInstructionsTableController {

    private MainAppController mainController;
    private HighlightSelectionModel highlightModel;
    private ProgramSelectorModel programSelectorModel;
    private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private TableColumn<InstructionDTO, Number> colIndex;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colInstruction;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;
    @FXML private TableColumn<InstructionDTO, Boolean> colBreakPoint;

    @FXML
    protected void initialize() {
        colBreakPoint.setCellValueFactory(new PropertyValueFactory<>("breakpoint"));
        colBreakPoint.setCellFactory(col -> new TableCell<InstructionDTO, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(event -> {
                    InstructionDTO rowItem = getTableView().getItems().get(getIndex());
                    if (rowItem != null) {
                        rowItem.setBreakpoint(checkBox.isSelected());
                    }
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    InstructionDTO rowItem = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(rowItem.isBreakpoint());
                    setGraphic(checkBox);
                }
            }
        });

        colIndex.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(instructionsTable.getItems().indexOf(param.getValue()) + 1)
        );

//        colIndex.setCellValueFactory(new PropertyValueFactory<>("instructionNumber"));
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
        this.currentSelectedProgramProperty = programProperty;
    }

    public void setModels(HighlightSelectionModel model, ProgramSelectorModel  programSelectorModel) {
        this.highlightModel = model;
        this.programSelectorModel = programSelectorModel;
        highlightSelectionOnMainTable();       // configure cell factories
    }

    public void initializeListeners() {
        // Listen to program changes and repopulate the table
        currentSelectedProgramProperty.addListener((obs, oldProg, newProgram) -> {
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


        programSelectorModel.selectedUserStringProperty().addListener((observableValue, oldProgram, newProgramName) -> {
            if (newProgramName != null) {
                instructionsTable.getItems().clear();
                ProgramDTO chosenProgram = mainController.getChosenProgramByUserString(newProgramName);
                instructionsTable.getItems().setAll(chosenProgram.getInstructions().getProgramInstructionsDtoList());

            }
        });
    }

    // Highlighting lines - only on the main table
    private void highlightSelectionOnMainTable() {
        if (highlightModel == null) return;

        // Refresh table when selection changes
        highlightModel.selectedHighlightProperty().addListener((obs, oldVal, newVal) -> instructionsTable.refresh());

        instructionsTable.setRowFactory(tv -> new TableRow<>() {
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
    
    public void highlightLineDebugMode(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < instructionsTable.getItems().size()) {
            instructionsTable.getSelectionModel().select(rowIndex);
            scrollToCenter(instructionsTable, rowIndex);
        }
    }

    public void turnOffHighlighting() {
        instructionsTable.getSelectionModel().clearSelection();
    }

    public void fillTable(InstructionsDTO instructions) {
        instructionsTable.getItems().setAll(instructions.getProgramInstructionsDtoList());
    }

    public List<Boolean> getBreakPoints() {
        return instructionsTable.getItems().stream()
                .map(InstructionDTO::isBreakpoint)
                .toList();
    }
}
