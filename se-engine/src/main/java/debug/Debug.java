package debug;

import dto.DebugDTO;
import dto.ProgramExecutorDTO;


public interface Debug {
    DebugDTO resume();
    DebugDTO stepOver();
    DebugDTO stepBack();

    boolean hasMoreInstructionsNotIncludingLast();

    ProgramExecutorDTO getDebugProgramExecutor();
    int getCurrentInstructionIndex();
}
