package components.execution.topToolBar;

import components.execution.mainExecution.MainExecutionController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import static components.execution.topToolBar.HighlightSelectionModelV3.EMPTY_CHOICE;

public class TopToolBarController {

    private MainExecutionController executionController;
    private HighlightSelectionModelV3 highlightSelectionModel;
    private final BooleanProperty lockExpandCollapseCB = new SimpleBooleanProperty(false);
    private final BooleanProperty lockHighlightCB = new SimpleBooleanProperty(false);
    private final BooleanProperty lockProgramSelectorCB = new SimpleBooleanProperty(false);

    @FXML private Label currentProgramName;
    @FXML private ComboBox<Integer> collapseCB;
    @FXML private Label currentDegreeLabel;
    @FXML private ComboBox<Integer> expandCB;
    @FXML private ComboBox<String> highlightSelectionCB;

    public void setExecutionController(MainExecutionController executionController) {
        this.executionController = executionController;
    }

    public void setModels(ExpansionCollapseModelV3 expansionCollapseModel, HighlightSelectionModelV3 highlightSelectionModel) {
        setupExpansionCollapseModel(expansionCollapseModel);
        setupHighlightSelection(highlightSelectionModel);
    }

    public void setProgramCurrentName(String programName) {
        currentProgramName.setText("Program Name: " + programName);
    }

    private void setupExpansionCollapseModel(ExpansionCollapseModelV3 expansionCollapseModel) {

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

        currentDegreeLabel.textProperty().bind(
                Bindings.concat(
                        expansionCollapseModel.currentDegreeProperty().asString(),
                        " / ",
                        expansionCollapseModel.maxDegreeProperty().asString()
                )
        );

        attachSelectionHandlerToComboBox(collapseCB);
        attachSelectionHandlerToComboBox(expandCB);

        // Use helper method to apply placeholder text
        configureExpandComboBoxDisplay(collapseCB, "Collapse");
        configureExpandComboBoxDisplay(expandCB, "Expand");
    }

    private void setupHighlightSelection(HighlightSelectionModelV3 highlightSelectionModel) {
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
                executionController.jumpToDegree(selectedDegree);
            }
        });
    }

    public void setComponentsDisabled(Boolean disable) {
        lockProgramSelectorCB.set(disable);
        lockExpandCollapseCB.set(disable);
        lockHighlightCB.set(disable);
    }

}

