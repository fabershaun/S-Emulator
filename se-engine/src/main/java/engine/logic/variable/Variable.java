package engine.logic.variable;

import java.io.Serializable;

public interface Variable extends Serializable {
    VariableType getType();
    String getRepresentation();
    int getNumber();
    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);
}
