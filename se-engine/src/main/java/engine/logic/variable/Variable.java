package engine.logic.variable;

import java.io.Serializable;

public interface Variable extends Serializable {
    VariableType type();
    String getRepresentation();
    int number();
    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);
}
