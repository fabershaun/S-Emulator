package dto;

public class DebugDTO {

    private final ProgramExecutorDTO debugProgramExecutorDTO;
    private final int instructionNumber;
    private final boolean hasMoreInstructions;

    public DebugDTO(ProgramExecutorDTO debugProgramExecutorDTO, int instructionNumber, int nextInstructionNumber, boolean hasMoreInstructions) {
        this.debugProgramExecutorDTO = debugProgramExecutorDTO;
        this.instructionNumber = instructionNumber;
        this.hasMoreInstructions = hasMoreInstructions;
    }

    public ProgramExecutorDTO getDebugProgramExecutorDTO() {
        return debugProgramExecutorDTO;
    }

    public int getCurrentInstructionNumber() {
        return instructionNumber;
    }

    public int getNextInstructionNumber() {
        return instructionNumber;
    }

    public boolean hasMoreInstructions() {
        return hasMoreInstructions;
    }

}
