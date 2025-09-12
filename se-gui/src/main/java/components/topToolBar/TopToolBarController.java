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

    @FXML private ComboBox<Integer> collapseCB;
    @FXML private Label currentDegreeLabel;
    @FXML private ComboBox<Integer> expandCB;
    @FXML private ComboBox<String> highlightSelectionCB;
    @FXML private ComboBox<?> programFunctionSelectorCB;

    @FXML
    public void initialize() {

    }

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setModels(ExpansionCollapseModel expansionCollapseModel, HighlightSelectionModel highlightSelectionModel) {
        setExpansionCollapseModel(expansionCollapseModel);
        setHighlightSelectionModel(highlightSelectionModel);
    }

    public void setExpansionCollapseModel(ExpansionCollapseModel expansionCollapseModel) {
        this.expansionCollapseModel = expansionCollapseModel;
        initializeExpansionModelBindings();            // Bind items, label, and disabled states to the model
        registerDegreeSelectionHandlers();    // Attach unified listeners for both ComboBoxes

        // Use helper method to apply placeholder text
        configureExpandComboBoxDisplay(collapseCB, "Collapse");
        configureExpandComboBoxDisplay(expandCB, "Expand");
    }

    public void setHighlightSelectionModel(HighlightSelectionModel highlightSelectionModel) {
        this.highlightSelectionModel =  highlightSelectionModel;
        initializeHighlightModelBindings();

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

        highlightSelectionCB.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean clear = newValue == null || EMPTY_CHOICE.equals(newValue);
            if (clear) {
                highlightSelectionModel.selectHighlight(null);
            } else {
                highlightSelectionModel.selectHighlight(newValue);
            }
        });
    }

    private void initializeExpansionModelBindings() {
        // Bind items to model-backed option lists
        collapseCB.setItems(expansionCollapseModel.getCollapseOptions());
        expandCB.setItems(expansionCollapseModel.getExpandOptions());

        // Keep the label in sync with the current degree value
        currentDegreeLabel.textProperty().bind(expansionCollapseModel.currentDegreeProperty().asString());

        // Disable ComboBoxes when there are no options available
        collapseCB.disableProperty().bind(Bindings.isEmpty(expansionCollapseModel.getCollapseOptions()));
        expandCB.disableProperty().bind(Bindings.isEmpty(expansionCollapseModel.getExpandOptions()));
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

    private void initializeHighlightModelBindings() {
        // Bind items to model-backed option lists
        highlightSelectionCB.setItems(highlightSelectionModel.getHighlightOptions());

        // Disable ComboBoxes when there are no options available
        highlightSelectionCB.disableProperty().bind(Bindings.isEmpty(highlightSelectionModel.getHighlightOptions()));
    }

    private void registerDegreeSelectionHandlers() {
        // Attach the same selection logic to both ComboBoxes
        attachSelectionHandlerToCombo(collapseCB);
        attachSelectionHandlerToCombo(expandCB);
    }

    private void attachSelectionHandlerToCombo(ComboBox<Integer> integerComboBox) {
        // React to user selection in a unified way
        integerComboBox.valueProperty().addListener((observableValue, previous, chosen) -> {
            if (chosen != null) {
                attemptJumpToDegreeAndClearSelection(chosen);
            }
        });
    }

    private void attemptJumpToDegreeAndClearSelection(int chosenDegree) {
        try {
            mainController.jumpToDegree(chosenDegree);
        } catch (EngineLoadException e) {
            e.printStackTrace();
        }
    }
}