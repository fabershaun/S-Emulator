package instruction;

import label.Label;
import variable.Variable;

public interface BasicInstruction {
    Instruction cloneInstruction(Variable variable, Label label, Label referencesLabel, Instruction origin, int instructionNumber);
}
