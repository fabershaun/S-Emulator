package components.mainApp;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public final class AppState {

    private final SimpleStringProperty selectedFilePathProperty;
    private final SimpleBooleanProperty isFileSelected;
    private final SimpleBooleanProperty isProgramSelected;
    private final SimpleBooleanProperty isFunctionSelected;
    private final SimpleBooleanProperty isExpandSelected;
    private final SimpleBooleanProperty isCollapseSelected;

    public  AppState() {
        selectedFilePathProperty = new SimpleStringProperty("");
        isFileSelected = new SimpleBooleanProperty(false);
        isProgramSelected = new SimpleBooleanProperty(true);
        isFunctionSelected = new SimpleBooleanProperty(false);
        isExpandSelected = new SimpleBooleanProperty(true);
        isCollapseSelected = new SimpleBooleanProperty(false);
    }

    public SimpleStringProperty selectedFilePathProperty() {
        return selectedFilePathProperty;
    }

    public SimpleBooleanProperty isFileSelectedProperty() {
        return isFileSelected;
    }

    public SimpleBooleanProperty isProgramSelectedProperty() {
        return isProgramSelected;
    }

    public SimpleBooleanProperty isFunctionSelectedProperty() {
        return isFunctionSelected;
    }

    public SimpleBooleanProperty isExpandSelectedProperty() {
        return isExpandSelected;
    }

    public SimpleBooleanProperty isCollapseSelectedProperty() {
        return isCollapseSelected;
    }
}
