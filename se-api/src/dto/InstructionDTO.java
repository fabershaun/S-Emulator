package dto;

public class InstructionDTO {
    private final String instructionName;
    private final int instructionNumber;            // Do not change name (name needed in gui module: package components.mainInstructionsTable)
    private final int cycleNumber;                  // Do not change name (name needed in gui module: package components.mainInstructionsTable)
    private final String instructionTypeStr;        // Do not change name (name needed in gui module: package components.mainInstructionsTable)
    private final String labelStr;                  // Do not change name (name needed in gui module: package components.mainInstructionsTable)
    private final String referenceLabelStr;
    private final String targetVariableStr;
    private final String sourceVariableStr;
    private final String command;                   // Do not change name (name needed in gui module: package components.mainInstructionsTable)
    private final InstructionDTO origin;

    public InstructionDTO(String instructionName,
                          int instructionNumber,
                          int cyclesNumber,
                          String instructionType,
                          String labelStr,
                          String referenceLabelStr,
                          String targetVariableStr,
                          String sourceVariableStr,
                          String command,
                          InstructionDTO origin
    ) {
        this.instructionName = instructionName;
        this.instructionNumber = instructionNumber;
        this.cycleNumber = cyclesNumber;
        this.instructionTypeStr = instructionType;
        this.labelStr = labelStr;
        this.referenceLabelStr = referenceLabelStr;
        this.targetVariableStr = targetVariableStr;
        this.sourceVariableStr = sourceVariableStr;
        this.command = command;
        this.origin = origin;
    }

    public String getInstructionName() {
        return instructionName;
    }

    public int getInstructionNumber() {
        return instructionNumber;
    }

    public int getCyclesNumber() {
        return cycleNumber;
    }

    public String getInstructionTypeStr() {
        return instructionTypeStr;
    }

    public String getLabelStr() {
        return labelStr;
    }

    public String getReferenceLabelStr() {
        return referenceLabelStr;
    }

    public String getTargetVariableStr() {
        return targetVariableStr;
    }

    public String getSourceVariableStr() {
        return sourceVariableStr;
    }

    public String getCommand() {
        return command;
    }

    public InstructionDTO getOrigin() {
        return origin;
    }
}


