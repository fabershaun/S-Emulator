package components.programCreation;

import components.mainApp.MainAppController;
import dto.v2.InstructionDTO;
import dto.v2.InstructionDataDTO;
import dto.v2.ProgramDTO;
import engine.logic.instruction.InstructionDataMapper;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static components.loadFile.LoadFileController.showEngineError;

public class ProgramCreationController {

    private final int WIDE = 100;
    private final int NORMAL = 70;

    @FXML private Button saveButton;
    @FXML private Button uploadToAppButton;

    @FXML private TextField programNameTF;
    @FXML private ComboBox<Integer> lineNumberChooserCB;
    @FXML private ComboBox<InstructionDataDTO> chooseInstructionCB;
    @FXML private VBox dynamicArgsBox;
    @FXML private TableView<InstructionDTO> instructionsTable;
    @FXML private Button deleteInstructionButton;

    private MainAppController mainController;
    private ProgramCreationModel programCreationModel;
    private final Map<String, Runnable> uiBuilders = new HashMap<>();
    private Path lastProgramCreatedPath;
    private Window ownerWindow;

    @FXML
    private void initialize() {
        disableEditing(true);
        saveButton.setDisable(true);
        uploadToAppButton.setDisable(true);
        lineNumberChooserCB.setDisable(true);
        deleteInstructionButton.setDisable(true);
        initializeListeners();
        initializeInstructionChoices();
        initBuilders();

        Tooltip lineNumberTooltip = new Tooltip("Select the line number in the program where this instruction will be inserted");
        lineNumberChooserCB.setTooltip(lineNumberTooltip);

        instructionsTable.getColumns().removeFirst();        // Remove the column of the break points
        instructionsTable.setPlaceholder(new Label("No instructions have been created"));
    }

    public void setModel(ProgramCreationModel model) {
        this.programCreationModel = model;
    }

    public void setMainController(MainAppController mainAppController) {
        this.mainController = mainAppController;
    }

    public void setOwnerWindow(Window ownerWindow) {
        this.ownerWindow = ownerWindow;
    }

    private void initializeInstructionChoices() {
        chooseInstructionCB.getItems().addAll(InstructionDataMapper.getAvailableInstructions());
    }

    private void initializeListeners() {
        // Enable save only if there is at least one instruction
        instructionsTable.getItems().addListener((ListChangeListener<InstructionDTO>) change -> {
            boolean empty = instructionsTable.getItems().isEmpty();
            saveButton.setDisable(empty);
            lineNumberChooserCB.setDisable(empty);

            refreshLineNumberChooser();
            lineNumberChooserCB.getSelectionModel().clearSelection();
            lineNumberChooserCB.setPromptText("Select instruction number");
        });

        // Enable delete only if a row is selected
        instructionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> deleteInstructionButton.setDisable(newSel == null));

        chooseInstructionCB.valueProperty().addListener((obs, oldVal, newVal) -> {
            dynamicArgsBox.getChildren().clear();

            if (newVal == null) return;

            buildDynamicArgsFields(newVal);
        });
    }

    private void initBuilders() {
        uiBuilders.put("INCREASE", this::buildIncreaseUI);
        uiBuilders.put("DECREASE", this::buildDecreaseUI);
        uiBuilders.put("JNZ", this::buildJnzUI);
        uiBuilders.put("NO_OP", this::buildNoOpUI);
        uiBuilders.put("ZERO_VARIABLE", this::buildZeroVariableUI);
        uiBuilders.put("GOTO_LABEL", this::buildGotoLabelUI);
        uiBuilders.put("ASSIGNMENT", this::buildAssignmentUI);
        uiBuilders.put("CONSTANT_ASSIGNMENT", this::buildConstantAssignmentUI);
        uiBuilders.put("JUMP_ZERO", this::buildJumpZeroUI);
        uiBuilders.put("JUMP_EQUAL_CONSTANT", this::buildJumpEqualConstantUI);
        uiBuilders.put("JUMP_EQUAL_VARIABLE", this::buildJumpEqualVariableUI);
    }

    private void refreshLineNumberChooser() {
        int size = instructionsTable.getItems().size();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= size + 1; i++) {
            numbers.add(i);
        }

        lineNumberChooserCB.getItems().setAll(numbers);
        lineNumberChooserCB.getSelectionModel().select(size + 1);   // Default choice is the next line number
    }

    private void buildDynamicArgsFields(InstructionDataDTO instructionDataDTO) {
        Runnable builder = uiBuilders.get(instructionDataDTO.getName().toUpperCase());
        if (builder != null) {
            builder.run();
        } else {
            throw new IllegalArgumentException(instructionDataDTO.getName().toUpperCase() + " is not a valid instruction.");
        }
    }

    private void disableEditing(boolean disabled) {
        programNameTF.setDisable(disabled);
        chooseInstructionCB.setDisable(disabled);
    }

    @FXML
    private void onNewProgram() {
        disableEditing(false);
        programCreationModel.resetEngine();
        instructionsTable.getItems().clear();
        programNameTF.clear();
        chooseInstructionCB.getSelectionModel().clearSelection();
        chooseInstructionCB.setPromptText("Choose instruction");
        lineNumberChooserCB.getSelectionModel().clearSelection();
        lineNumberChooserCB.setPromptText("Select instruction number");
    }

    @FXML
    private void onUploadFileToEdit() {
        programCreationModel.resetEngine();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Program");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml")
        );

        File file = fileChooser.showOpenDialog(saveButton.getScene().getWindow());

        if (file != null) {
            try {
                ProgramDTO loaded = programCreationModel.loadProgramFromFile(file.toPath());
                instructionsTable.getItems().setAll(loaded.getInstructions().getProgramInstructionsDtoList());
                saveButton.setDisable(false);
                disableEditing(false);
            } catch (Exception e) {
                showEngineError("Failed to load program", e.getMessage());
            }
        }    }

    @FXML
    void onDeleteClicked() {
        // Get the selected instruction
        InstructionDTO selected = instructionsTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // Remove it from the table
            instructionsTable.getItems().remove(selected);

            // Set the indexes
            reindexInstructions();
            // Clear the selection
            instructionsTable.getSelectionModel().clearSelection();
        }
    }

    @FXML
    void onSaveClicked() {
        List<InstructionDTO> instructions = new ArrayList<>(instructionsTable.getItems());
        String programName = programNameTF.getText();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Program");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml")
        );
        fileChooser.setInitialFileName(
                (programName != null && !programName.isBlank()) ? programName + ".xml" : "new_program.xml"
        );

        File file = fileChooser.showSaveDialog(saveButton.getScene().getWindow());

        if (file == null) {
            // User pressed Cancel, so just return without doing anything
            return;
        }

        try {
            programCreationModel.saveProgramToFile(file, programName, instructions);
            uploadToAppButton.setDisable(false);
            this.lastProgramCreatedPath = file.toPath();
        } catch (Exception e) {
            showEngineError("Failed to save program", e.getMessage());
        }
    }

    @FXML
    void onUploadToAppButtonClicked(ActionEvent event) {
        mainController.loadNewFile(lastProgramCreatedPath, ownerWindow);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private TextField createNumericField() {
        TextField textField = new TextField();
        textField.setPromptText("Target Variable");

        textField.setPrefWidth(NORMAL);
        textField.setMaxWidth(NORMAL);

        // Only integers
        TextFormatter<Integer> formatter = new TextFormatter<>(
                change -> change.getControlNewText().matches("-?\\d*") ? change : null
        );
        textField.setTextFormatter(formatter);
        textField.setFocusTraversable(true);

        return textField;
    }

    private HBox createLabelField() {
        TextField textField = new TextField();
        textField.setPromptText("Label index");

        textField.setPrefWidth(WIDE);
        textField.setMaxWidth(WIDE);

        Label label = new Label();
        label.setFocusTraversable(false); // skip label on TAB

        // Whenever user types a number, update label to "L" + number
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.matches("\\d+")) {
                label.setText("L" + newVal);
            } else {
                label.setText("");
            }
        });

        HBox hBox = new HBox(5, textField, label); // spacing of 5px between field and label
        hBox.setSpacing(5);

        hBox.setUserData(label);

        return hBox;
    }

    // Build the whole UI for unary operations (INCREASE / DECREASE)
    private void buildUnaryOperationUI(String instructionName) {
        HBox labelIndexBox = createLabelIndexBox();
        ComboBox<String> variableTypeCB = new ComboBox<>();
        TextField variableNumberField = createNumericField();
        variableTypeCB.setPrefWidth(WIDE);

        Label variablePreview = new Label();

        // Prepare variable row with behavior
        HBox variableRow = createVariableRow(variableTypeCB, variableNumberField, variablePreview);

        // Add button
        Button addButton = createUnaryAddButton(
                instructionName,
                labelIndexBox,
                variableTypeCB,
                variableNumberField,
                variablePreview
        );

        addButton.setFocusTraversable(false); // skip button on TAB

        dynamicArgsBox.setSpacing(8);
        dynamicArgsBox.getChildren().addAll(labelIndexBox, variableRow, addButton);
    }


// ----------------------- Helpers -----------------------

    private void addInstructionAtChosenLine(InstructionDTO instructionDTO) {
        Integer chosenLine = lineNumberChooserCB.getValue();

        if (chosenLine == null) {
            instructionsTable.getItems().add(instructionDTO);
        } else {
            int index = Math.max(0, chosenLine - 1);

            if (index >= instructionsTable.getItems().size()) {
                instructionsTable.getItems().add(instructionDTO);
            } else {    // add to the middle
                instructionsTable.getItems().add(index, instructionDTO);
                reindexInstructions();
            }
        }
    }

    private void reindexInstructions() {
        List<InstructionDTO> items = instructionsTable.getItems();
        for (int i = 0; i < items.size(); i++) {
            InstructionDTO instructionDTO = items.get(i);
            instructionDTO.setInstructionNumber(i + 1);
        }
        instructionsTable.refresh();
    }

    // Creates the "Label index" row
    private HBox createLabelIndexBox() {
        return createLabelField(); // Already have your helper
    }

    // Creates the variable row: ComboBox + TextField + Preview
    private HBox createVariableRow(ComboBox<String> variableTypeCB,
                                   TextField variableNumberField,
                                   Label variablePreview) {

        variableTypeCB.getItems().addAll("x", "y", "z");
        variableTypeCB.setPromptText("Type");

        variableNumberField.setPromptText("Number");

// Behavior when type changes
        variableTypeCB.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Y".equalsIgnoreCase(newVal)) {
                variableNumberField.setDisable(true);
                variableNumberField.clear();
                variablePreview.setText("y");
            } else if ("X".equalsIgnoreCase(newVal) || "Z".equalsIgnoreCase(newVal)) {
                variableNumberField.setDisable(false);
                String number = variableNumberField.getText();
                variablePreview.setText(newVal.toLowerCase() + (number.isEmpty() ? "" : number));
            } else {
                variableNumberField.setDisable(true);
                variablePreview.setText("");
            }
        });

// Behavior when number changes
        variableNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            String type = variableTypeCB.getValue();
            if ("X".equalsIgnoreCase(type) || "Z".equalsIgnoreCase(type)) {
                variablePreview.setText(type.toLowerCase() + newVal);
            }
        });

        return new HBox(8, variableTypeCB, variableNumberField, variablePreview);
    }

    // Creates the "Add Instruction" button with logic
    private Button createUnaryAddButton(String instructionName,
                                        HBox labelIndexBox,
                                        ComboBox<String> variableTypeCB,
                                        TextField variableNumberField,
                                        Label variablePreview) {
        Button addButton = new Button("Add Instruction");
        addButton.setDisable(true);



        // Validation logic
        Runnable validateFields = () -> {
            String type = variableTypeCB.getValue();
            String number = variableNumberField.getText();

            boolean valid = false;

            if (type != null) {
                if ("y".equalsIgnoreCase(type)) {
                    // For Y, no number is required
                    valid = true;
                } else if ("x".equalsIgnoreCase(type) || "z".equalsIgnoreCase(type)) {
                    // For X/Z, require a number
                    valid = !number.isBlank();
                }
            }

            addButton.setDisable(!valid);
        };

        // Add listeners
        variableTypeCB.valueProperty().addListener((obs, o, n) -> validateFields.run());
        variableNumberField.textProperty().addListener((obs, o, n) -> validateFields.run());

        // Action
        addButton.setOnAction(ev -> {
            Label labelNode = (Label) labelIndexBox.getUserData();
            String label = labelNode.getText();
            String variableType = variableTypeCB.getValue();
            String variableNumber = variableNumberField.getText();

            String targetVarStr = (variableType != null)
                    ? variableType + (variableType.equalsIgnoreCase("Y") ? "" : variableNumber)
                    : "";

            InstructionDTO instructionDTO;
            switch (instructionName) {
                case "INCREASE" ->
                        instructionDTO = programCreationModel.createIncrease(targetVarStr, label);
                case "DECREASE" ->
                        instructionDTO = programCreationModel.createDecrease(targetVarStr, label);
                case "NO_OP" ->
                        instructionDTO = programCreationModel.createNoOp(targetVarStr, label);
                case "ZERO_VARIABLE" ->
                        instructionDTO = programCreationModel.createZeroVariable(targetVarStr, label);
                default -> throw new IllegalArgumentException("Unsupported: " + instructionName);
            }

            addInstructionAtChosenLine(instructionDTO);

            // Clear fields
            ((TextField) labelIndexBox.getChildren().getFirst()).clear();
            variableTypeCB.getSelectionModel().clearSelection();
            variableNumberField.clear();
            variablePreview.setText("");

            addButton.setDisable(true);
        });

        return addButton;
    }


    private void buildIncreaseUI() {
        buildUnaryOperationUI("INCREASE");
    }

    private void buildDecreaseUI() {
        buildUnaryOperationUI("DECREASE");
    }

    // Build the whole UI for JNZ
    private void buildJnzUI() {
        // Label index row
        HBox labelIndexBox = createLabelIndexBox();

        // Variable row (type + number)
        ComboBox<String> variableTypeCB = new ComboBox<>();
        TextField variableNumberField = createNumericField();
        variableTypeCB.setPrefWidth(WIDE);
        Label variablePreview = new Label();
        HBox variableRow = createVariableRow(variableTypeCB, variableNumberField, variablePreview);

        // Reference label field (like normal label)
        HBox referenceLabelBox = createLabelField();
        TextField referenceLabelField = (TextField) referenceLabelBox.getChildren().getFirst();
        referenceLabelField.setPromptText("Reference Label index");
        Label referenceLabelPreview = (Label) referenceLabelBox.getUserData();

        variableTypeCB.setFocusTraversable(true);
        variableNumberField.setFocusTraversable(true);
        referenceLabelField.setFocusTraversable(true);

        // Add button with validation
        Button addButton = new Button("Add Instruction");
        addButton.setDisable(true);
        addButton.setFocusTraversable(false);

        Runnable validateFields = () -> {
            boolean valid = false;
            String type = variableTypeCB.getValue();
            String number = variableNumberField.getText();
            String ref = referenceLabelPreview.getText(); // use the "L<num>" label

            if (type != null && ref != null && !ref.isBlank()) {
                if ("y".equalsIgnoreCase(type)) {
                    valid = true;
                } else if ("x".equalsIgnoreCase(type) || "z".equalsIgnoreCase(type)) {
                    valid = !number.isBlank();
                }
            }

            addButton.setDisable(!valid);
        };

        variableTypeCB.valueProperty().addListener((obs, o, n) -> validateFields.run());
        variableNumberField.textProperty().addListener((obs, o, n) -> validateFields.run());
        referenceLabelField.textProperty().addListener((obs, o, n) -> validateFields.run());

        addButton.setOnAction(ev -> {
            Label labelNode = (Label) labelIndexBox.getUserData();
            String label = labelNode.getText();
            String variableType = variableTypeCB.getValue();
            String variableNumber = variableNumberField.getText();
            String referenceLabel = referenceLabelPreview.getText(); // use "L<num>"

            String targetVarStr = (variableType != null)
                    ? variableType + (variableType.equalsIgnoreCase("Y") ? "" : variableNumber)
                    : "";

            InstructionDTO instructionDTO = programCreationModel.createJnz(
                    targetVarStr,
                    label,
                    referenceLabel
            );

            addInstructionAtChosenLine(instructionDTO);

            // Clear fields
            ((TextField) labelIndexBox.getChildren().getFirst()).clear();
            variableTypeCB.getSelectionModel().clearSelection();
            variableNumberField.clear();
            variablePreview.setText("");
            referenceLabelField.clear();
            referenceLabelPreview.setText("");

            addButton.setDisable(true);
        });

        // Add all UI rows
        dynamicArgsBox.setSpacing(8);
        dynamicArgsBox.getChildren().addAll(labelIndexBox, variableRow, referenceLabelBox, addButton);
    }

    private void buildNoOpUI() {
        buildUnaryOperationUI("NO_OP");
    }

    private void buildZeroVariableUI() {
        buildUnaryOperationUI("ZERO_VARIABLE");
    }

    // Build the whole UI for GOTO_LABEL
    private void buildGotoLabelUI() {
        // Target label (optional)
        HBox labelIndexBox = createLabelIndexBox();

        // Reference label (mandatory, same mechanism as target label)
        HBox referenceLabelBox = createLabelIndexBox();
        TextField referenceLabelField = (TextField) referenceLabelBox.getChildren().getFirst();
        Label referenceLabelPreview = (Label) referenceLabelBox.getUserData();
        referenceLabelField.setPromptText("Reference Label");
        referenceLabelField.setFocusTraversable(true);

        // Add button with validation
        Button addButton = new Button("Add Instruction");
        addButton.setDisable(true);
        addButton.setFocusTraversable(false);

        Runnable validateFields = () -> {
            String ref = referenceLabelField.getText();
            boolean valid = ref != null && !ref.isBlank();
            addButton.setDisable(!valid);
        };

        referenceLabelField.textProperty().addListener((obs, o, n) -> validateFields.run());

        addButton.setOnAction(ev -> {

            Label labelNode = (Label) labelIndexBox.getUserData();
            String targetLabel = labelNode.getText();

            String referenceLabel = referenceLabelPreview.getText();

            InstructionDTO instructionDTO = programCreationModel.createGotoLabel(
                    targetLabel,
                    referenceLabel
            );

            addInstructionAtChosenLine(instructionDTO);

            // Clear fields
            ((TextField) labelIndexBox.getChildren().getFirst()).clear();
            referenceLabelField.clear();

            addButton.setDisable(true);
        });

        // Add all UI rows
        dynamicArgsBox.setSpacing(8);
        dynamicArgsBox.getChildren().addAll(labelIndexBox, referenceLabelBox, addButton);
    }

    // Build the whole UI for ASSIGNMENT
    private void buildAssignmentUI() {
        HBox labelIndexBox = createLabelIndexBox();

        // Target variable row
        ComboBox<String> targetVarTypeCB = new ComboBox<>();
        TextField targetVarNumberField = createNumericField();
        targetVarTypeCB.setPrefWidth(WIDE);
        Label targetVarPreview = new Label();
        HBox targetVarRow = createVariableRow(targetVarTypeCB, targetVarNumberField, targetVarPreview);

        // Reference variable row
        ComboBox<String> refVarTypeCB = new ComboBox<>();
        TextField refVarNumberField = createNumericField();
        refVarTypeCB.setPrefWidth(WIDE);
        Label refVarPreview = new Label();
        HBox refVarRow = createVariableRow(refVarTypeCB, refVarNumberField, refVarPreview);

        targetVarTypeCB.setFocusTraversable(true);
        targetVarNumberField.setFocusTraversable(true);
        refVarTypeCB.setFocusTraversable(true);
        refVarNumberField.setFocusTraversable(true);

        // Add button
        Button addButton = new Button("Add Instruction");
        addButton.setDisable(true);
        addButton.setFocusTraversable(false);

        // Validation: require both target + reference
        Runnable validateFields = () -> {
            boolean validTarget = false;
            boolean validRef = false;

            String targetType = targetVarTypeCB.getValue();
            String targetNum = targetVarNumberField.getText();

            if (targetType != null) {
                if ("y".equalsIgnoreCase(targetType)) {
                    validTarget = true;
                } else if ("x".equalsIgnoreCase(targetType) || "z".equalsIgnoreCase(targetType)) {
                    validTarget = !targetNum.isBlank();
                }
            }

            String refType = refVarTypeCB.getValue();
            String refNum = refVarNumberField.getText();

            if (refType != null) {
                if ("y".equalsIgnoreCase(refType)) {
                    validRef = true;
                } else if ("x".equalsIgnoreCase(refType) || "z".equalsIgnoreCase(refType)) {
                    validRef = !refNum.isBlank();
                }
            }

            addButton.setDisable(!(validTarget && validRef));
        };

        targetVarTypeCB.valueProperty().addListener((obs, o, n) -> validateFields.run());
        targetVarNumberField.textProperty().addListener((obs, o, n) -> validateFields.run());
        refVarTypeCB.valueProperty().addListener((obs, o, n) -> validateFields.run());
        refVarNumberField.textProperty().addListener((obs, o, n) -> validateFields.run());

        // Action
        addButton.setOnAction(ev -> {
            Label labelNode = (Label) labelIndexBox.getUserData();
            String label = labelNode.getText();

            String targetVarStr = (targetVarTypeCB.getValue() != null)
                    ? targetVarTypeCB.getValue() + (targetVarTypeCB.getValue().equalsIgnoreCase("Y") ? "" : targetVarNumberField.getText())
                    : "";

            String refVarStr = (refVarTypeCB.getValue() != null)
                    ? refVarTypeCB.getValue() + (refVarTypeCB.getValue().equalsIgnoreCase("Y") ? "" : refVarNumberField.getText())
                    : "";

            InstructionDTO instructionDTO = programCreationModel.createAssignment(
                    targetVarStr,
                    refVarStr,
                    label
            );

            addInstructionAtChosenLine(instructionDTO);

            // Clear fields
            ((TextField) labelIndexBox.getChildren().getFirst()).clear();
            targetVarTypeCB.getSelectionModel().clearSelection();
            targetVarNumberField.clear();
            targetVarPreview.setText("");
            refVarTypeCB.getSelectionModel().clearSelection();
            refVarNumberField.clear();
            refVarPreview.setText("");

            addButton.setDisable(true);
        });

        // Add UI rows
        dynamicArgsBox.setSpacing(8);
        dynamicArgsBox.getChildren().addAll(labelIndexBox, targetVarRow, refVarRow, addButton);
    }

    // Build the whole UI for CONSTANT_ASSIGNMENT
    private void buildConstantAssignmentUI() {
        // Label index row
        HBox labelIndexBox = createLabelIndexBox();

        // Target Variable row (ComboBox + Number + Preview)
        ComboBox<String> variableTypeCB = new ComboBox<>();
        TextField variableNumberField = createNumericField();
        variableTypeCB.setPrefWidth(WIDE);
        Label variablePreview = new Label();
        HBox variableRow = createVariableRow(variableTypeCB, variableNumberField, variablePreview);

        // Constant field (only positive integers)
        TextField constantField = new TextField();
        constantField.setPromptText("Constant");
        constantField.setPrefWidth(WIDE);
        constantField.setMaxWidth(WIDE);

        variableTypeCB.setFocusTraversable(true);
        variableNumberField.setFocusTraversable(true);
        constantField.setFocusTraversable(true);

        TextFormatter<Integer> constantFormatter = new TextFormatter<>(
                change -> change.getControlNewText().matches("\\d*") ? change : null
        );
        constantField.setTextFormatter(constantFormatter);


        // Add button with validation
        Button addButton = new Button("Add Instruction");
        addButton.setDisable(true);
        addButton.setFocusTraversable(false);

        Runnable validateFields = () -> {
            String type = variableTypeCB.getValue();
            String number = variableNumberField.getText();
            String constant = constantField.getText();

            boolean valid = false;
            if (type != null && constant != null && !constant.isBlank()) {
                if ("y".equalsIgnoreCase(type)) {
                    valid = true;
                } else if ("x".equalsIgnoreCase(type) || "z".equalsIgnoreCase(type)) {
                    valid = !number.isBlank();
                }
            }
            addButton.setDisable(!valid);
        };

        variableTypeCB.valueProperty().addListener((obs, o, n) -> validateFields.run());
        variableNumberField.textProperty().addListener((obs, o, n) -> validateFields.run());
        constantField.textProperty().addListener((obs, o, n) -> validateFields.run());

        addButton.setOnAction(ev -> {
            Label labelNode = (Label) labelIndexBox.getUserData();
            String label = labelNode.getText();
            String variableType = variableTypeCB.getValue();
            String variableNumber = variableNumberField.getText();
            long constant = Long.parseLong(constantField.getText());

            String targetVarStr = (variableType != null)
                    ? variableType + (variableType.equalsIgnoreCase("Y") ? "" : variableNumber)
                    : "";

            InstructionDTO instructionDTO = programCreationModel.createConstantAssignment(
                    targetVarStr,
                    label,
                    constant
            );

            addInstructionAtChosenLine(instructionDTO);

            // Clear fields
            ((TextField) labelIndexBox.getChildren().getFirst()).clear();
            variableTypeCB.getSelectionModel().clearSelection();
            variableNumberField.clear();
            variablePreview.setText("");
            constantField.clear();

            addButton.setDisable(true);
        });

        // Add all UI rows
        dynamicArgsBox.setSpacing(8);
        dynamicArgsBox.getChildren().addAll(labelIndexBox, variableRow, constantField, addButton);
    }

    // Build the whole UI for JUMP_ZERO
    private void buildJumpZeroUI() {
        // Label index row
        HBox labelIndexBox = createLabelIndexBox();

        // Target Variable row (type + number)
        ComboBox<String> variableTypeCB = new ComboBox<>();
        TextField variableNumberField = createNumericField();
        variableTypeCB.setPrefWidth(WIDE);
        Label variablePreview = new Label();
        HBox variableRow = createVariableRow(variableTypeCB, variableNumberField, variablePreview);

        // Reference Label (mandatory, with "L + number" like other labels)
        HBox referenceLabelBox = createLabelField();
        TextField referenceLabelField = (TextField) referenceLabelBox.getChildren().get(0);
        referenceLabelField.setPromptText("Reference Label index");
        Label referenceLabelPreview = (Label) referenceLabelBox.getChildren().get(1);

        variableTypeCB.setFocusTraversable(true);
        variableNumberField.setFocusTraversable(true);
        referenceLabelField.setFocusTraversable(true);

        // Add button with validation
        Button addButton = new Button("Add Instruction");
        addButton.setDisable(true);
        addButton.setFocusTraversable(false);

        Runnable validateFields = () -> {
            String type = variableTypeCB.getValue();
            String number = variableNumberField.getText();
            String ref = referenceLabelField.getText();

            boolean valid = false;
            if (type != null && ref != null && !ref.isBlank()) {
                if ("y".equalsIgnoreCase(type)) {
                    valid = true;
                } else if ("x".equalsIgnoreCase(type) || "z".equalsIgnoreCase(type)) {
                    valid = !number.isBlank();
                }
            }
            addButton.setDisable(!valid);
        };

        variableTypeCB.valueProperty().addListener((obs, o, n) -> validateFields.run());
        variableNumberField.textProperty().addListener((obs, o, n) -> validateFields.run());
        referenceLabelField.textProperty().addListener((obs, o, n) -> validateFields.run());

        addButton.setOnAction(ev -> {
            Label labelNode = (Label) labelIndexBox.getUserData();
            String label = labelNode.getText();
            String variableType = variableTypeCB.getValue();
            String variableNumber = variableNumberField.getText();
            String referenceLabel = referenceLabelPreview.getText();

            String targetVarStr = (variableType != null)
                    ? variableType + (variableType.equalsIgnoreCase("Y") ? "" : variableNumber)
                    : "";

            InstructionDTO instructionDTO = programCreationModel.createJumpZero(
                    targetVarStr,
                    label,
                    referenceLabel
            );

            addInstructionAtChosenLine(instructionDTO);

            // Clear fields
            ((TextField) labelIndexBox.getChildren().getFirst()).clear();
            variableTypeCB.getSelectionModel().clearSelection();
            variableNumberField.clear();
            variablePreview.setText("");
            referenceLabelField.clear();
            referenceLabelPreview.setText("");

            addButton.setDisable(true);
        });

        // Add all UI rows
        dynamicArgsBox.setSpacing(8);
        dynamicArgsBox.getChildren().addAll(labelIndexBox, variableRow, referenceLabelBox, addButton);
    }

    // Build the whole UI for JUMP_EQUAL_CONSTANT
    private void buildJumpEqualConstantUI() {
        // Label index row
        HBox labelIndexBox = createLabelIndexBox();

        // Target Variable row (type + number)
        ComboBox<String> variableTypeCB = new ComboBox<>();
        TextField variableNumberField = createNumericField();
        variableTypeCB.setPrefWidth(WIDE);
        Label variablePreview = new Label();
        HBox variableRow = createVariableRow(variableTypeCB, variableNumberField, variablePreview);

        // Constant field (positive integers only)
        TextField constantField = new TextField();
        constantField.setPromptText("Constant");
        constantField.setPrefWidth(WIDE);
        constantField.setMaxWidth(WIDE);
        constantField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        // Reference Label (mandatory, same as other labels)
        HBox referenceLabelBox = createLabelField();
        TextField referenceLabelField = (TextField) referenceLabelBox.getChildren().get(0);
        referenceLabelField.setPromptText("Reference Label index");
        Label referenceLabelPreview = (Label) referenceLabelBox.getChildren().get(1);

        variableTypeCB.setFocusTraversable(true);
        variableNumberField.setFocusTraversable(true);
        constantField.setFocusTraversable(true);
        referenceLabelField.setFocusTraversable(true);

        // Add button with validation
        Button addButton = new Button("Add Instruction");
        addButton.setDisable(true);
        addButton.setFocusTraversable(false);

        Runnable validateFields = () -> {
            String type = variableTypeCB.getValue();
            String number = variableNumberField.getText();
            String constant = constantField.getText();
            String ref = referenceLabelField.getText();

            boolean valid = false;
            if (type != null && !constant.isBlank() && ref != null && !ref.isBlank()) {
                if ("y".equalsIgnoreCase(type)) {
                    valid = true;
                } else if ("x".equalsIgnoreCase(type) || "z".equalsIgnoreCase(type)) {
                    valid = !number.isBlank();
                }
            }
            addButton.setDisable(!valid);
        };

        variableTypeCB.valueProperty().addListener((obs, o, n) -> validateFields.run());
        variableNumberField.textProperty().addListener((obs, o, n) -> validateFields.run());
        constantField.textProperty().addListener((obs, o, n) -> validateFields.run());
        referenceLabelField.textProperty().addListener((obs, o, n) -> validateFields.run());

        addButton.setOnAction(ev -> {
            Label labelNode = (Label) labelIndexBox.getUserData();
            String label = labelNode.getText();
            String variableType = variableTypeCB.getValue();
            String variableNumber = variableNumberField.getText();
            long constant = Long.parseLong(constantField.getText());
            String referenceLabel = referenceLabelPreview.getText();

            String targetVarStr = (variableType != null)
                    ? variableType + (variableType.equalsIgnoreCase("Y") ? "" : variableNumber)
                    : "";

            InstructionDTO instructionDTO = programCreationModel.createJumpEqualConstant(
                    targetVarStr,
                    label,
                    constant,
                    referenceLabel
            );

            addInstructionAtChosenLine(instructionDTO);

            // Clear fields
            ((TextField) labelIndexBox.getChildren().getFirst()).clear();
            variableTypeCB.getSelectionModel().clearSelection();
            variableNumberField.clear();
            variablePreview.setText("");
            constantField.clear();
            referenceLabelField.clear();
            referenceLabelPreview.setText("");

            addButton.setDisable(true);
        });

        // Add all UI rows
        dynamicArgsBox.setSpacing(8);
        dynamicArgsBox.getChildren().addAll(
                labelIndexBox,
                variableRow,
                constantField,
                referenceLabelBox,
                addButton
        );
    }

    // Build the whole UI for JUMP_EQUAL_VARIABLE
    private void buildJumpEqualVariableUI() {
        HBox labelIndexBox = createLabelIndexBox();

        // Target Variable row
        ComboBox<String> targetTypeCB = new ComboBox<>();
        TextField targetNumberField = createNumericField();
        targetTypeCB.setPrefWidth(WIDE);
        Label targetPreview = new Label();
        HBox targetRow = createVariableRow(targetTypeCB, targetNumberField, targetPreview);

        // Reference Variable row
        ComboBox<String> refTypeCB = new ComboBox<>();
        TextField refNumberField = createNumericField();
        refTypeCB.setPrefWidth(WIDE);
        Label refPreview = new Label();
        HBox refRow = createVariableRow(refTypeCB, refNumberField, refPreview);

        // Reference Label row (mandatory)
        HBox referenceLabelBox = createLabelField();
        TextField referenceLabelField = (TextField) referenceLabelBox.getChildren().get(0);
        Label referenceLabelPreview = (Label) referenceLabelBox.getChildren().get(1);
        referenceLabelField.setPromptText("Reference Label");

        targetTypeCB.setFocusTraversable(true);
        targetNumberField.setFocusTraversable(true);
        refTypeCB.setFocusTraversable(true);
        refNumberField.setFocusTraversable(true);
        referenceLabelField.setFocusTraversable(true);

        // Add button
        Button addButton = new Button("Add Instruction");
        addButton.setDisable(true);
        addButton.setFocusTraversable(false);

        // Validation: require target var + ref var + ref label
        Runnable validateFields = () -> {
            boolean targetValid = false;
            boolean refValid = false;
            boolean labelValid = referenceLabelPreview.getText() != null && !referenceLabelPreview.getText().isBlank();

            String targetType = targetTypeCB.getValue();
            String targetNumber = targetNumberField.getText();

            if (targetType != null) {
                if ("y".equalsIgnoreCase(targetType)) {
                    targetValid = true;
                } else if ("x".equalsIgnoreCase(targetType) || "z".equalsIgnoreCase(targetType)) {
                    targetValid = !targetNumber.isBlank();
                }
            }

            String refType = refTypeCB.getValue();
            String refNumber = refNumberField.getText();

            if (refType != null) {
                if ("y".equalsIgnoreCase(refType)) {
                    refValid = true;
                } else if ("x".equalsIgnoreCase(refType) || "z".equalsIgnoreCase(refType)) {
                    refValid = !refNumber.isBlank();
                }
            }

            addButton.setDisable(!(targetValid && refValid && labelValid));
        };

        targetTypeCB.valueProperty().addListener((obs, o, n) -> validateFields.run());
        targetNumberField.textProperty().addListener((obs, o, n) -> validateFields.run());
        refTypeCB.valueProperty().addListener((obs, o, n) -> validateFields.run());
        refNumberField.textProperty().addListener((obs, o, n) -> validateFields.run());
        referenceLabelField.textProperty().addListener((obs, o, n) -> validateFields.run());

        // Action
        addButton.setOnAction(ev -> {
            Label labelNode = (Label) labelIndexBox.getUserData();
            String label = labelNode.getText();

            String targetVarStr = (targetTypeCB.getValue() != null)
                    ? targetTypeCB.getValue() + (targetTypeCB.getValue().equalsIgnoreCase("Y") ? "" : targetNumberField.getText())
                    : "";

            String refVarStr = (refTypeCB.getValue() != null)
                    ? refTypeCB.getValue() + (refTypeCB.getValue().equalsIgnoreCase("Y") ? "" : refNumberField.getText())
                    : "";

            String referenceLabel = referenceLabelPreview.getText();

            InstructionDTO instructionDTO = programCreationModel.createJumpEqualVariable(
                    targetVarStr,
                    label,
                    refVarStr,
                    referenceLabel
            );

            addInstructionAtChosenLine(instructionDTO);

            // Clear fields
            ((TextField) labelIndexBox.getChildren().getFirst()).clear();
            targetTypeCB.getSelectionModel().clearSelection();
            targetNumberField.clear();
            targetPreview.setText("");
            refTypeCB.getSelectionModel().clearSelection();
            refNumberField.clear();
            refPreview.setText("");
            referenceLabelField.clear();
            referenceLabelPreview.setText("");

            addButton.setDisable(true);
        });

        // Add all UI rows
        dynamicArgsBox.setSpacing(8);
        dynamicArgsBox.getChildren().addAll(
                labelIndexBox,
                targetRow,
                refRow,
                referenceLabelBox,
                addButton
        );
    }
}
