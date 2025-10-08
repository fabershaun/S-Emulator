package engine.logic.execution;

import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;

public interface ExecutionContext {
    void initializeVariables(Program program, Long... inputs);
    long getVariableValue(Variable variable);
    void updateVariable(Variable variable, long value);
}
