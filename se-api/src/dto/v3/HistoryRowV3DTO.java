package dto.v3;

import java.util.List;
import java.util.Map;

public class HistoryRowV3DTO {

    private final String programType;
    private final String programName;
    private final String programUserString;
    private final String architectureChoice;
    private final int degree;
    private final long result;
    private final long totalCycles;
    private final Map<String, Long> variablesToValuesSorted;
    private final List<Long> inputsValues;

    public HistoryRowV3DTO(String programType, String programName, String programUserString, String architectureChoice, int degree, long result, long totalCycles, Map<String, Long> variablesToValuesSorted, List<Long> inputsValues) {
        this.programType = programType;
        this.programName = programName;
        this.programUserString = programUserString;
        this.architectureChoice = architectureChoice;
        this.degree = degree;
        this.result = result;
        this.totalCycles = totalCycles;
        this.variablesToValuesSorted = variablesToValuesSorted;
        this.inputsValues = inputsValues;
    }

    public String getProgramType() {
        return programType;
    }
    public String getProgramName() {
        return programName;
    }
    public String getProgramUserString() {
        return programUserString;
    }
    public String getArchitectureChoice() {
        return architectureChoice;
    }
    public int getDegree() { return degree; }
    public long getResult() { return result; }
    public long getTotalCycles() { return totalCycles; }
    public Map<String, Long> getVariablesToValuesSorted() { return variablesToValuesSorted; }
    public List<Long> getInputsValuesOfUser() { return inputsValues; }
}
