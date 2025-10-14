package dto.v2;

public class InstructionDTO {
    private final String instructionName;
    private int instructionNumber;                  // Do not change name (name needed in gui module: package components.execution.mainInstructionsTable)
    private final int cycleNumber;                  // Do not change name (name needed in gui module: package components.execution.mainInstructionsTable)
    private final String instructionTypeStr;        // Do not change name (name needed in gui module: package components.execution.mainInstructionsTable)
    private final String labelStr;                  // Do not change name (name needed in gui module: package components.execution.mainInstructionsTable)
    private final String command;                   // Do not change name (name needed in gui module: package components.execution.mainInstructionsTable)
    private final String referenceLabelStr;
    private final String targetVariableStr;
    private final String sourceVariableStr;
    private final long constantValue;
    private final InstructionDTO origin;
    private final String architectureStr;              // Do not change name (name needed in gui module: package components.execution.mainInstructionsTable)
    private final int architectureRank;

    private boolean breakpoint;                     // Do not change name (name needed in gui module: package components.execution.mainInstructionsTable)

    public InstructionDTO(String instructionName,
                          int instructionNumber,
                          int cyclesNumber,
                          String instructionType,
                          String labelStr,
                          String referenceLabelStr,
                          String targetVariableStr,
                          String sourceVariableStr,
                          long constantValue,
                          String command,
                          InstructionDTO origin,
                          String architectureStr,
                          int architectureRank
    ) {
        this.instructionName = instructionName;
        this.instructionNumber = instructionNumber;
        this.cycleNumber = cyclesNumber;
        this.instructionTypeStr = instructionType;
        this.labelStr = labelStr;
        this.referenceLabelStr = referenceLabelStr;
        this.targetVariableStr = targetVariableStr;
        this.sourceVariableStr = sourceVariableStr;
        this.constantValue = constantValue;
        this.command = command;
        this.origin = origin;
        this.architectureStr = architectureStr;
        this.architectureRank = architectureRank;
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

    public long getConstantValue() {
        return constantValue;
    }

    public String getCommand() {
        return command;
    }

    public InstructionDTO getOrigin() {
        return origin;
    }

    public String getArchitectureStr() {
        return architectureStr;
    }

    public int getArchitectureRank() {
        return architectureRank;
    }

    public boolean isBreakpoint() {
        return breakpoint;
    }   // Used for break point

    public void setBreakpoint(boolean breakpoint) {
        this.breakpoint = breakpoint;
    }   // Used for break point

    public void setInstructionNumber(int newInstructionNumber) {
        this.instructionNumber = newInstructionNumber;
    }
}


