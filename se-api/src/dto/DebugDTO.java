package dto;

public class DebugDTO {

    private final ProgramExecutorDTO debugProgramExecutorDTO;
    private final int currentInstructionNumber;
    private final int nextInstructionNumber;
    private final boolean hasMoreInstructions;
    private final String targetVariable ;

    public DebugDTO(ProgramExecutorDTO debugProgramExecutorDTO,
                    int instructionNumber,
                    int nextInstructionNumber,
                    boolean hasMoreInstructions,
                    String targetVariable) {
        this.debugProgramExecutorDTO = debugProgramExecutorDTO;
        this.currentInstructionNumber = instructionNumber;
        this.nextInstructionNumber = nextInstructionNumber;
        this.hasMoreInstructions = hasMoreInstructions;
        this.targetVariable = targetVariable;
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

    public String getTargetVariable() {
        return targetVariable;
    }

    public boolean hasMoreInstructions() {
        return hasMoreInstructions;
    }

}
