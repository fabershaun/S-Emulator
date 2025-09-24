package dto;

import java.util.Map;

public class DebugDTO {
    private final Map<String, Long> VariablesToValuesSorted;
    private final long result;
    private final int instructionNumber;
    private final int currentCycles;
    private final boolean hasMoreInstructions;

    public DebugDTO(Map<String, Long> variablesToValuesSorted, long result, int instructionNumber, int currentCycles, boolean hasMoreInstructions) {
        this.VariablesToValuesSorted = variablesToValuesSorted;
        this.result = result;
        this.instructionNumber = instructionNumber;
        this.currentCycles = currentCycles;
        this.hasMoreInstructions = hasMoreInstructions;
    }

    public Map<String, Long> getVariablesToValuesSorted() {
        return VariablesToValuesSorted;
    }

    public long getResult() {
        return result;
    }

    public int getInstructionNumber() {
        return instructionNumber;
    }

    public int getCurrentCycles() {
        return currentCycles;
    }

    public boolean hasMoreInstructions() {
        return hasMoreInstructions;
    }
}
