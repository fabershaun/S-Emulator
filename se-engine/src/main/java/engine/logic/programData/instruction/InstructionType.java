package engine.logic.programData.instruction;

public enum InstructionType {

    BASIC("B"),
    SYNTHETIC("S")
    ;

    private final String instructionType;

    InstructionType(String instructionType) {
        this.instructionType = instructionType;
    }

    String getInstructionType() {
        return instructionType;
    }
}