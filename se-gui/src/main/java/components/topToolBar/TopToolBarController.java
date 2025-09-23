package components.topToolBar;

import components.mainApp.MainAppController;
import exceptions.EngineLoadException;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import static components.topToolBar.HighlightSelectionModel.EMPTY_CHOICE;

public class TopToolBarController {

    private MainAppController mainController;
    private ExpansionCollapseModel expansionCollapseModel;
    private HighlightSelectionModel highlightSelectionModel;
    private ProgramSelectorModel programSelectorModel;

    @FXML private ComboBox<Integer> collapseCB;
    @FXML private Label currentDegreeLabel;
    @FXML private ComboBox<Integer> expandCB;
    @FXML private ComboBox<String> highlightSelectionCB;
    @FXML private ComboBox<String> programFunctionSelectorCB;
    @FXML


    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setModels(ExpansionCollapseModel expansionCollapseModel, HighlightSelectionModel highlightSelectionModel, ProgramSelectorModel programSelectorModel) {
        setupExpansionCollapseModel(expansionCollapseModel);
        setupHighlightSelection(highlightSelectionModel);
        setupProgramAndFunctionsSelectorModel(programSelectorModel);
    }

    public void setupExpansionCollapseModel(ExpansionCollapseModel expansionCollapseModel) {
        this.expansionCollapseModel = expansionCollapseModel;

        collapseCB.setItems(expansionCollapseModel.getCollapseOptions());
        expandCB.setItems(expansionCollapseModel.getExpandOptions());

        collapseCB.disableProperty().bind(Bindings.isEmpty(expansionCollapseModel.getCollapseOptions())); // Disable ComboBoxes when there are no options available
        expandCB.disableProperty().bind(Bindings.isEmpty(expansionCollapseModel.getExpandOptions()));
        currentDegreeLabel.textProperty().bind(expansionCollapseModel.currentDegreeProperty().asString());

        attachSelectionHandlerToComboBox(collapseCB);
        attachSelectionHandlerToComboBox(expandCB);

        // Use helper method to apply placeholder text
        configureExpandComboBoxDisplay(collapseCB, "Collapse");
        configureExpandComboBoxDisplay(expandCB, "Expand");
    }

    public void setupHighlightSelection(HighlightSelectionModel highlightSelectionModel) {
        this.highlightSelectionModel =  highlightSelectionModel;

        highlightSelectionCB.setItems(highlightSelectionModel.getHighlightOptions());  // Bind items to model-backed option lists
        highlightSelectionCB.disableProperty().bind(Bindings.isEmpty(highlightSelectionModel.getHighlightOptions()));  // Disable ComboBoxes when there are no options available

        attachHighlightSelectionListener();
        configureHighlightComboBoxDisplay();
    }

    public void setupProgramAndFunctionsSelectorModel(ProgramSelectorModel programSelectorModel) {
        this.programSelectorModel = programSelectorModel;

        programSelectorModel.setMainController(mainController);

        programFunctionSelectorCB.setItems(programSelectorModel.getProgramAndFunctionsOptions());
        programFunctionSelectorCB.disableProperty().bind(Bindings.isNull(programSelectorModel.currentProgramProperty()));  // Disable ComboBoxes when there are no program loaded

        attachProgramFunctionSelectionListener();
        configureProgramSelectionComboBoxDisplay();
    }

    private void attachProgramFunctionSelectionListener() {
        programFunctionSelectorCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            programSelectorModel.selectProgramOrFunction(newValue);
        });
    }

    private void configureProgramSelectionComboBoxDisplay() {
        programFunctionSelectorCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText("Program \\ Function Selector");
                } else {
                    setText(item);
                }
            }
        });
    }

    private void configureHighlightComboBoxDisplay() {
        highlightSelectionCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || EMPTY_CHOICE.equals(item)) {
                    setText("Highlight Selection");
                } else {
                    setText(item);
                }
            }
        });
    }

    private void attachHighlightSelectionListener() {
        highlightSelectionCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean clear = newValue == null || EMPTY_CHOICE.equals(newValue);
            highlightSelectionModel.selectHighlight(clear ? null : newValue);
        });
    }

//    private void initializeExpansionModelBindings() {
//    }
//    private void initializeHighlightModelBindings() {
//    }
//    private void initializeProgramSelectorModelBindings() {
//    }

    private void configureExpandComboBoxDisplay(ComboBox<Integer> comboBox, String placeholder) {
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(placeholder); // Always show placeholder text
            }
        });
    }

    private void attachSelectionHandlerToComboBox(ComboBox<Integer> integerComboBox) {
        // React to user selection in a unified way
        integerComboBox.valueProperty().addListener((observableValue, previous, selectedDegree) -> {
            if (selectedDegree != null) {
                try {
                    mainController.jumpToDegree(selectedDegree);
                } catch (EngineLoadException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}