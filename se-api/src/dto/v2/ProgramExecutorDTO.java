package dto.v2;

import java.util.List;
import java.util.Map;

public class ProgramExecutorDTO {
    private final ProgramDTO programDTO;
    private final Map<String, Long> VariablesToValuesSorted;
    private final long result;
    private final int totalCycles;
    private final int degree;
    private final List<Long> inputs;
    private String architectureTypeSelected;


    public ProgramExecutorDTO(ProgramDTO programDTO, Map<String, Long> VariablesToValuesSorted, long result, int totalCycles, int degree, List<Long> inputs, String architectureTypeSelected) {
        this.programDTO = programDTO;
        this.VariablesToValuesSorted = VariablesToValuesSorted;
        this.result = result;
        this.totalCycles = totalCycles;
        this.degree = degree;
        this.inputs = inputs;
        this.architectureTypeSelected = architectureTypeSelected;
    }

    public ProgramDTO getProgramDTO() {
        return programDTO;
    }

    public Map<String, Long> getVariablesToValuesSorted() {
        return VariablesToValuesSorted;
    }

    public long getResult() { return result; }
    public int getTotalCycles() { return totalCycles; }
    public int getDegree() { return degree; }

    public List<Long> getInputsValuesOfUser() {
        return inputs;
    }

    public String getArchitectureTypeSelected() { return architectureTypeSelected; }
}
