package components.execution.debuggerExecutionMenu;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VariableRowV3 {
    private final StringProperty variableName;
    private final LongProperty variableValue;

    public VariableRowV3(String name, Long value) {
        this.variableName = new SimpleStringProperty(name);
        this.variableValue = new SimpleLongProperty(value);
    }

    public StringProperty variableNameProperty() {   // To update UI when value change. need it
        return variableName;
    }

    public LongProperty variableValueProperty() {   // To update UI when value change. need it
        return variableValue;
    }

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

