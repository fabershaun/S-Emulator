package instruction;

import instruction.basic.JumpNotZeroInstruction;
import label.Label;
import variable.Variable;

import java.util.List;

public interface SyntheticInstruction {
    List<Instruction> getInnerInstructions();
    int getMaxDegree();

    int setInnerInstructionsAndReturnTheNextOne(int startNumber);

    Instruction CloneInstruction(Variable targetVariable, Label label, Variable sourceVariable, long constantValue, Label referencesLabel, Instruction origin, int instructionNumber);
}

