package dto;

import java.util.List;
import java.util.Map;

public class ProgramExecutorDTO {
    private final ProgramDTO programDTO;
    private final Map<String, Long> VariablesToValuesSorted;
    private final long result;                      // Do not change name (name needed in gui module: package components.mainInstructionsTable)
    private final int totalCycles;                  // Do not change name (name needed in gui module: package components.mainInstructionsTable)
    private final int degree;                       // Do not change name (name needed in gui module: package components.mainInstructionsTable)
    private final List<Long> inputs;


    public ProgramExecutorDTO(ProgramDTO programDTO, Map<String, Long> VariablesToValuesSorted, long result, int totalCycles, int degree, List<Long> inputs) {
        this.programDTO = programDTO;
        this.VariablesToValuesSorted = VariablesToValuesSorted;
        this.result = result;
        this.totalCycles = totalCycles;
        this.degree = degree;
        this.inputs = inputs;
    }

    public ProgramDTO getProgramDTO() {
        return programDTO;
    }

    public Map<String, Long> getVariablesToValuesSorted() {
        return VariablesToValuesSorted;
    }

    public long getResult() {
        return result;
    }

    public int getCycles() {
        return totalCycles;
    }

    public int getDegree() {
        return degree;
    }

    public List<Long> getInputsValuesOfUser() {
        return inputs;
    }
}
