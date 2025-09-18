package execution;

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

}
