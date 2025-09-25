package dto;

public class DebugDTO {

    private final ProgramExecutorDTO debugProgramExecutorDTO;
    private final int currentInstructionNumber;
    private final int nextInstructionNumber;
    private final boolean hasMoreInstructions;

    public DebugDTO(ProgramExecutorDTO debugProgramExecutorDTO, int instructionNumber, int nextInstructionNumber, boolean hasMoreInstructions) {
        this.debugProgramExecutorDTO = debugProgramExecutorDTO;
        this.currentInstructionNumber = instructionNumber;
        this.nextInstructionNumber = nextInstructionNumber;
        this.hasMoreInstructions = hasMoreInstructions;
    }

    public ProgramExecutorDTO getDebugProgramExecutorDTO() {
        return debugProgramExecutorDTO;
    }

    public int getCurrentInstructionNumber() {
        return currentInstructionNumber;
    }

    public int getNextInstructionNumber() {
        return nextInstructionNumber;
    }

    public boolean hasMoreInstructions() {
        return hasMoreInstructions;
    }

}
