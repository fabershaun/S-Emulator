package debug;

import dto.DebugDTO;
import dto.ProgramExecutorDTO;
import execution.ProgramExecutor;

import java.util.List;


public interface Debug {
    DebugDTO resume(List<Boolean> breakPoints);
    DebugDTO stepOver();
    DebugDTO stepBack();
    DebugDTO stop();

    boolean hasMoreInstructions();

    ProgramExecutorDTO buildProgramExecutorDTO(ProgramExecutor programExecutor);
    ProgramExecutor getDebugProgramExecutor();
    int getCurrentInstructionIndex();
    int getNextInstructionIndex();

}
