package engine.logic.label;

import java.io.Serializable;

public interface Label extends Serializable {
    String getLabelRepresentation();
    int getNumber();
}
