package execution;

import debug.Debug;
import program.Program;
import variable.Variable;

import java.util.List;
import java.util.Map;

public interface ProgramExecutor {

    void run(int runDegree, Long... inputs);
    Program getProgram();
    long getVariableValue(Variable variable);
    int getRunDegree();
    List<Long> getInputsValuesOfUser();
    int getTotalCyclesOfProgram();
    Map<String, Long> getVariablesToValuesSorted();

    void setRunDegree(int runDegree);
    void setTotalCycles(int totalCycles);
    void setExecutionContext(ExecutionContext executionContext);
    void setInputsValues(List<Long> inputsValues);
}
