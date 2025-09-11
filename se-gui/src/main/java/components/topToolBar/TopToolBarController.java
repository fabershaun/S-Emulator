package components.topToolBar;

import components.mainApp.MainAppController;
import dto.ProgramDTO;
import exceptions.EngineLoadException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

public class TopToolBarController {

    private MainAppController mainController;
    private ExpansionCollapseModel model;

    @FXML private ComboBox<Integer> collapseCB;
    @FXML private Label currentDegreeLabel;
    @FXML private ComboBox<Integer> expandCB;
    @FXML private ComboBox<?> highlightSelectionCB;
    @FXML private ComboBox<?> programFunctionSelectorCB;

    public void setMainController(MainAppController mainController) {
        this.mainController = mainController;
    }

    public void setModel(ExpansionCollapseModel model) {
        this.model = model;
        initializeModelBindings();            // Bind items, label, and disabled states to the model
        registerDegreeSelectionHandlers();    // Attach unified listeners for both ComboBoxes
    }

    private void initializeModelBindings() {
        // Bind items to model-backed option lists
        collapseCB.setItems(model.getCollapseOptions());
        expandCB.setItems(model.getExpandOptions());

        // Keep the label in sync with the current degree value
        currentDegreeLabel.textProperty().bind(model.currentDegreeProperty().asString());

        // Disable ComboBoxes when there are no options available
        collapseCB.disableProperty().bind(Bindings.isEmpty(model.getCollapseOptions()));
        expandCB.disableProperty().bind(Bindings.isEmpty(model.getExpandOptions()));

        // Use helper method to apply placeholder text
        configureComboBoxDisplay(collapseCB, "Collapse");
        configureComboBoxDisplay(expandCB, "Expand");
    }

    private void configureComboBoxDisplay(ComboBox<Integer> comboBox, String placeholder) {
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(placeholder); // Always show placeholder text
            }
        });

        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);   // Dont display an empty line
                } else {
                    setText(item.toString());
                }
            }
        });
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