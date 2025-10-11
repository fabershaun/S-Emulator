package engine.logic.execution.runMode;

import dto.v3.UserDTO;
import engine.logic.execution.ExecutionContext;
import engine.logic.programData.architecture.ArchitectureType;
import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;

import java.util.List;
import java.util.Map;

public interface ProgramExecutor {

    void run(UserDTO userDTO, int runDegree, Long... inputs);
    Program getProgram();
    long getVariableValue(Variable variable);
    int getRunDegree();
    List<Long> getInputsValuesOfUser();
    int getTotalCycles();
    Map<String, Long> getVariablesToValuesSorted();
    ArchitectureType getArchitectureTypeSelected();

    void setRunDegree(int runDegree);
    void setTotalCycles(int totalCycles);
    void setExecutionContext(ExecutionContext executionContext);
    void setInputsValues(List<Long> inputsValues);
}
