package dto;

import java.util.Map;

public class DebugDTO {

    private final ProgramExecutorDTO debugProgramExecutorDTO;
    private final int instructionNumber;
    private final boolean hasMoreInstructions;

    public DebugDTO(ProgramExecutorDTO debugProgramExecutorDTO, int instructionNumber, boolean hasMoreInstructions) {
        this.debugProgramExecutorDTO = debugProgramExecutorDTO;
        this.instructionNumber = instructionNumber;
        this.hasMoreInstructions = hasMoreInstructions;
    }

    public ProgramExecutorDTO getDebugProgramExecutorDTO() {
        return debugProgramExecutorDTO;
    }

    public int getInstructionNumber() {
        return instructionNumber;
    }

    public boolean hasMoreInstructions() {
        return hasMoreInstructions;
    }

}
