package service;

import java.util.List;

public class ProgramRunRequest {
    public final String programName;
    public final int degree;
    public final String architecture;
    public final String username;
    public final List<Long> inputValues;

    public ProgramRunRequest(String programName, int degree, String architecture, String username, List<Long> inputValues) {
        this.programName = programName;
        this.degree = degree;
        this.architecture = architecture;
        this.username = username;
        this.inputValues = inputValues;
    }
}
