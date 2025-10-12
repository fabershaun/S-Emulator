package service;

import java.util.Map;

public class ProgramRunRequest {
    public final String programName;
    public final int degree;
    public final String architecture;
    public final String username;
    public final Map<String, String> inputValues;

    public ProgramRunRequest(String programName, int degree, String architecture, String username, Map<String, String> inputValues) {
        this.programName = programName;
        this.degree = degree;
        this.architecture = architecture;
        this.username = username;
        this.inputValues = inputValues == null ? Map.of() : Map.copyOf(inputValues);
    }
}
