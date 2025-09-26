package instruction;

import java.util.List;

public interface SyntheticInstruction {
    List<Instruction> getInnerInstructions();
    int expandInstruction(int startNumber); // And return the next instruction number
}

