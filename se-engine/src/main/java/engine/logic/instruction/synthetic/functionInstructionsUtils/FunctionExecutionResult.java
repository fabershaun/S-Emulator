package engine.logic.instruction.synthetic.functionInstructionsUtils;

import java.util.List;

public record FunctionExecutionResult(long resultValue, int cycles) {

    public static Long[] extractInputValues(List<FunctionExecutionResult> results) {
        // Map each FunctionExecutionResult to its value and collect into Long[]
        return results.stream()
                .map(FunctionExecutionResult::resultValue) // extract value
                .toArray(Long[]::new);                       // build Long[]
    }

    public static int addFunctionArgumentCycles(List<FunctionExecutionResult> functionExecutionResultList) {
        int cycles = 0;

        for (FunctionExecutionResult functionExecutionResult : functionExecutionResultList) {
            cycles += functionExecutionResult.cycles();
        }

        return cycles;
    }
}
