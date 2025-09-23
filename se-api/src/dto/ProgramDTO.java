package dto;

import java.util.List;

public class ProgramDTO {
    private final String programName;
    private final List<String> labelsStr;
    private final List<String> inputVariables;
    private final List<String> workVariables;
    private final InstructionsDTO instructions;
    private final List<List<InstructionDTO>> expandedProgram;

    public ProgramDTO(
            String programName,
            List<String> labelsStr,
            List<String> inputVariables,
            List<String> workVariables,
            InstructionsDTO instructions,
            List<List<InstructionDTO>> expandedProgram
    ) {
        this.programName = programName;
        this.labelsStr = labelsStr;
        this.inputVariables = inputVariables;
        this.workVariables = workVariables;
        this.instructions = instructions;
        this.expandedProgram = expandedProgram;
    }

    public String getProgramName() {
        return programName;
    }

    public List<String> getLabelsStr() {
        return labelsStr;
    }

    public List<String> getInputVariables() {
        return inputVariables;
    }

    public List<String> getWorkVariables() {
        return workVariables;
    }

    public InstructionsDTO getInstructions() {
        return instructions;
    }

    public List<List<InstructionDTO>> getExpandedProgram() {
        return expandedProgram;
    }
}
