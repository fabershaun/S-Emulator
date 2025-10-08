package engine.logic.execution;

import engine.logic.program.Program;
import engine.logic.variable.Variable;

public interface ExecutionContext {
    void initializeVariables(Program program, Long... inputs);
    long getVariableValue(Variable variable);
    void updateVariable(Variable variable, long value);
}
