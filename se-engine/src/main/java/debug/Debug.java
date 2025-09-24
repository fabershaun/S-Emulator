package debug;

import dto.DebugDTO;
import dto.ProgramExecutorDTO;
import execution.ProgramExecutor;
import program.Program;

import java.util.Map;

public interface Debug {
    DebugDTO resume();
    DebugDTO stepOver();
    DebugDTO stepBack();

    boolean hasMoreInstructionsNotIncludingLast();

    ProgramExecutorDTO getDebugProgramExecutor();
    Map<String, Long> getVariablesToValuesSorted();
    int getCurrentInstructionIndex();
    int getCurrentCycles();
}
