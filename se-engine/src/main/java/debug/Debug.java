package debug;

import dto.DebugDTO;

import java.util.Map;

public interface Debug {
    DebugDTO resume();
    DebugDTO stepOver();
    DebugDTO stepBack();

    boolean hasMoreInstructions();
    Map<String, Long> getVariablesToValuesSorted();
    long getResult();
    int getCurrentInstructionIndex();
    int getCurrentCycles();
}
