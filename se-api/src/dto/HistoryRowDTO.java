package dto;

import java.util.List;
import java.util.Map;

public class HistoryRowDTO {
    private final int runNumber;
    private final int degree;
    private final long result;
    private final long totalCycles;
    private final Map<String, Long> variablesToValuesSorted;
    private final List<Long> inputsValues;

    public HistoryRowDTO(int runNumber, int degree, long result, long totalCycles, Map<String, Long> variablesToValuesSorted, List<Long> inputsValues) {
        this.runNumber = runNumber;
        this.degree = degree;
        this.result = result;
        this.totalCycles = totalCycles;
        this.variablesToValuesSorted = variablesToValuesSorted;
        this.inputsValues = inputsValues;
    }

    public int getRunNumber() { return runNumber; }
    public int getDegree() { return degree; }
    public long getResult() { return result; }
    public long getTotalCycles() { return totalCycles; }
    public Map<String, Long> getVariablesToValuesSorted() { return variablesToValuesSorted; }
    public List<Long> getInputsValuesOfUser() { return inputsValues; }
}