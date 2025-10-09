package engine.logic.programData.program;

public enum ProgramType {
    MAIN("Main Program"),
    FUNCTION("Function")
    ;

    private final String programType;

    ProgramType(String programType) {
        this.programType = programType;
    }

    public String getType() {
        return programType;
    }
}
