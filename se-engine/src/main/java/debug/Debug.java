package debug;

import dto.DebugDTO;
import dto.ProgramExecutorDTO;
import execution.ProgramExecutor;


public interface Debug {
    DebugDTO resume();
    DebugDTO stepOver();
    DebugDTO stepBack();
    DebugDTO stop();

    boolean hasMoreInstructionsNotIncludingLast();

    ProgramExecutorDTO getDebugProgramExecutorDTO();
    ProgramExecutor getDebugProgramExecutor();
    int getCurrentInstructionIndex();

}
