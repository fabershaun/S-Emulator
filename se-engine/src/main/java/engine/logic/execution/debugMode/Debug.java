package engine.logic.execution.debugMode;

import dto.v2.DebugDTO;
import dto.v2.ProgramExecutorDTO;
import engine.logic.execution.runMode.ProgramExecutor;

import java.util.List;


public interface Debug {
    DebugDTO resume(List<Boolean> breakPoints) throws InterruptedException;
    DebugDTO stepOver();
    DebugDTO stepBack();
    DebugDTO stop();

    boolean hasMoreInstructions();

    ProgramExecutorDTO buildProgramExecutorDTO(ProgramExecutor programExecutor);
    ProgramExecutor getDebugProgramExecutor();
    int getCurrentInstructionIndex();
    int getNextInstructionIndex();

}
