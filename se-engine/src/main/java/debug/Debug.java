package debug;

import dto.DebugDTO;

import java.util.Map;

public interface Debug {
    DebugDTO resume();
    DebugDTO stepOver();
    DebugDTO stepBack();

    boolean hasMoreInstructionsNotIncludingLast();
    Map<String, Long> getVariablesToValuesSorted();
    int getCurrentInstructionIndex();
    int getCurrentCycles();
}
