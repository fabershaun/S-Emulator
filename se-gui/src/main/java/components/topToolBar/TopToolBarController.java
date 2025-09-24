package components.topToolBar;

import components.mainApp.MainAppController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    private final BooleanProperty lockExpandCollapseCB = new SimpleBooleanProperty(false);
    private final BooleanProperty lockHighlightCB = new SimpleBooleanProperty(false);
    private final BooleanProperty lockProgramSelectorCB = new SimpleBooleanProperty(false);

    @FXML private ComboBox<String> programSelectorCB;
    @FXML private ComboBox<Integer> collapseCB;
    @FXML private Label currentDegreeLabel;
    @FXML private ComboBox<Integer> expandCB;
    @FXML private ComboBox<String> highlightSelectionCB;


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

        collapseCB.disableProperty().bind(
                Bindings.or(
                        Bindings.isEmpty(expansionCollapseModel.getCollapseOptions()),
                        lockExpandCollapseCB
                )
        );

        expandCB.disableProperty().bind(
                Bindings.or(
                        Bindings.isEmpty(expansionCollapseModel.getExpandOptions()),
                        lockExpandCollapseCB
                )
        );

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
        highlightSelectionCB.disableProperty().bind(
                Bindings.or(
                        Bindings.isEmpty(highlightSelectionModel.getHighlightOptions()),  // Disable ComboBoxes when there are no options available
                        lockHighlightCB
                )
        );

        attachHighlightSelectionListener();
        configureHighlightComboBoxDisplay();
    }

    public void setupProgramAndFunctionsSelectorModel(ProgramSelectorModel programSelectorModel) {
        this.programSelectorModel = programSelectorModel;

        programSelectorModel.setMainController(mainController);

        programSelectorCB.setItems(programSelectorModel.getProgramAndFunctionsOptions());
        programSelectorCB.disableProperty().bind(
                Bindings.or(
                        Bindings.isEmpty(programSelectorModel.getProgramAndFunctionsOptions()),
                        lockProgramSelectorCB
                )
        );

        attachProgramFunctionSelectionListener();
        configureProgramSelectionComboBoxDisplay();
    }

    private void attachProgramFunctionSelectionListener() {
        programSelectorCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            programSelectorModel.setSelectedProgram(newValue);
        });
    }

    private void configureProgramSelectionComboBoxDisplay() {
        programSelectorCB.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText("Program Selector");
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
                mainController.jumpToDegree(selectedDegree);
            }
        });
    }

    public void setComponentsDisabled(Boolean disable) {
        lockProgramSelectorCB.set(disable);
        lockExpandCollapseCB.set(disable);
        lockHighlightCB.set(disable);
    }
}