package instruction.synthetic.functionInstructionsUtils;

import java.util.List;

public class FunctionExecutionResult {
    private final long resultValue;
    private final int cycles;

    public FunctionExecutionResult(long resultValue, int cycles) {
        this.resultValue = resultValue;
        this.cycles = cycles;
    }

    public long getResultValue() {
        return resultValue;
    }
    public int getCycles() {
        return cycles;
    }

    public static Long[] extractInputValues(List<FunctionExecutionResult> results) {
        // Map each FunctionExecutionResult to its value and collect into Long[]
        return results.stream()
                .map(FunctionExecutionResult::getResultValue) // extract value
                .toArray(Long[]::new);                       // build Long[]
    }

    public static int addFunctionArgumentCycles(List<FunctionExecutionResult> functionExecutionResultList) {
        int cycles = 0;

        for (FunctionExecutionResult functionExecutionResult : functionExecutionResultList) {
            cycles += functionExecutionResult.getCycles();
        }

        return cycles;
    }
}
