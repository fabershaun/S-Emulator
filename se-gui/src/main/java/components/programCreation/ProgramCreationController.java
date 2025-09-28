package components.programCreation;

import components.mainInstructionsTable.MainInstructionsTableController;
import dto.InstructionDTO;
import dto.InstructionDataDTO;
import instruction.InstructionDataMapper;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProgramCreationController {

    @FXML private Button createButton;
    @FXML private Button saveButton;

    @FXML private TextField programNameTF;
    @FXML private ComboBox<InstructionDataDTO> chooseInstructionCB;

    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private MainInstructionsTableController instructionsTableController;          // must: field name = fx:id + "Controller"
    @FXML private Button deleteInstructionButton;

    ContextMenu creationMenu;

    // הערות:
    // הכפתורים save ידלק רק כשיש לפחות פקודה אחת בטבלה
    // הכפתור delete ידלק רק כשפקודה מסויימת בטבלה תיבחר

    @FXML
    private void initialize() {
        disableEditing(true);
        saveButton.setDisable(true);
        deleteInstructionButton.setDisable(true);
        initializeListeners();
        initializeCreateMenu();
        initializeInstructionChoices();
    }

    private void initializeInstructionChoices() {
        chooseInstructionCB.getItems().addAll(InstructionDataMapper.getAvailableInstructions());
    }

    private void initializeListeners() {
        // Enable save only if there is at least one instruction
        instructionsTable.getItems().addListener((ListChangeListener<InstructionDTO>) change -> {
            saveButton.setDisable(instructionsTable.getItems().isEmpty());
        });

        // Enable delete only if a row is selected
        instructionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
             deleteInstructionButton.setDisable(newSel == null);
        });
    }

    private void initializeCreateMenu() {
        creationMenu = new ContextMenu();

        // Create menu items
        MenuItem newProgramItem = new MenuItem("New");
        MenuItem uploadFileItem = new MenuItem("Upload file");

        newProgramItem.setOnAction(event -> {
            disableEditing(false);
        });

        uploadFileItem.setOnAction(event -> {
            // TODO: Handle uploading a program from file
        });
    }

    private void disableEditing(boolean disabled) {
        programNameTF.setDisable(disabled);
        chooseInstructionCB.setDisable(disabled);
    }

    @FXML void OnCreateClicked(ActionEvent event) {
        creationMenu.show(createButton, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    @FXML void OnDeleteClicked(ActionEvent event) {

    }

    @FXML void OnSaveClicked(ActionEvent event) {

    }
}
