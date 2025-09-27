package dto;

import java.util.List;
import java.util.Map;

public class DebugDTO {
    private final String programName;
    private final int currentInstructionNumber;
    private final int nextInstructionNumber;
    private final boolean hasMoreInstructions;
    private final String targetVariable ;

    private final int degree;
    private final long result;
    private final int totalCycles;
    private final Map<String, Long> variablesToValuesSorted;

    public DebugDTO(String programName,
                    int currentInstructionNumber,
                    int nextInstructionNumber,
                    boolean hasMoreInstructions,
                    String targetVariable,
                    int degree,
                    long result,
                    int totalCycles,
                    Map<String, Long> variablesToValuesSorted) {

        this.programName = programName;
        this.currentInstructionNumber = currentInstructionNumber;
        this.nextInstructionNumber = nextInstructionNumber;
        this.hasMoreInstructions = hasMoreInstructions;
        this.targetVariable = targetVariable;
        this.degree = degree;
        this.result = result;
        this.totalCycles = totalCycles;
        this.variablesToValuesSorted = variablesToValuesSorted;
    }

    public String getProgramName() { return programName; }

    public int getCurrentInstructionNumber() {
        return currentInstructionNumber;
    }

    public int getNextInstructionNumber() {
        return nextInstructionNumber;
    }

    public String getTargetVariable() {
        return targetVariable;
    }

    public boolean hasMoreInstructions() {
        return hasMoreInstructions;
    }

    public int getDegree() {
        return degree;
    }

    public long getResult() {
        return result;
    }

    public int getTotalCycles() {
        return totalCycles;
    }

    public Map<String, Long> getVariablesToValuesSorted() {
        return variablesToValuesSorted;
    }
}
