package engine.logic.programData.label;

import java.io.Serializable;

public interface Label extends Serializable {
    String getLabelRepresentation();
    int getNumber();
}
