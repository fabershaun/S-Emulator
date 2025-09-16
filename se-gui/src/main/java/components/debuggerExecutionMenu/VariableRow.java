package components.debuggerExecutionMenu;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VariableRow {
    private final StringProperty variableName;
    private final LongProperty variableValue;

    public VariableRow(String name, Long value) {
        this.variableName = new SimpleStringProperty(name);
        this.variableValue = new SimpleLongProperty(value);
    }

//    public StringProperty variableNameProperty() {
//        return variableName;
//    }
//
//    public LongProperty variableValueProperty() {
//        return variableValue;
//    }

    public String getVariableName(){
        return variableName.get();
    }

    public long getVariableValue(){
        return variableValue.get();
    }

    public void setVariableValue(long newValue) {
        variableValue.set(newValue);
    }
}
