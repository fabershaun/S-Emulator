package dto;

import java.util.Map;

public class DebugDTO {

    private final ProgramExecutorDTO debugProgramExecutorDTO;
//    private final Map<String, Long> variablesToValuesSorted;
//    private final int currentCycles;
//    private final Map<String, Long> VariablesToValuesSorted;
//    private final int instructionNumber;
//    private final int currentCycles;
//    private final boolean hasMoreInstructions;

    public DebugDTO(ProgramExecutorDTO debugProgramExecutorDTO/*, Map<String, Long> variablesToValuesSorted, int instructionNumber, int currentCycles, boolean hasMoreInstructions*/) {
        this.debugProgramExecutorDTO = debugProgramExecutorDTO;
//        this.VariablesToValuesSorted = variablesToValuesSorted;
//        this.instructionNumber = instructionNumber;
//        this.currentCycles = currentCycles;
//        this.hasMoreInstructions = hasMoreInstructions;
    }

    public ProgramExecutorDTO getDebugProgramExecutorDTO() {
        return debugProgramExecutorDTO;
    }

//    public Map<String, Long> getVariablesToValuesSorted() {
//        return VariablesToValuesSorted;
//    }
//
//    public int getInstructionNumber() {
//        return instructionNumber;
//    }
//
//    public int getCurrentCycles() {
//        return currentCycles;
//    }
//
//    public boolean hasMoreInstructions() {
//        return hasMoreInstructions;
//    }
}
