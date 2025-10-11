package engine.logic.execution.runMode;

import dto.v3.UserDTO;
import engine.logic.execution.ExecutionContext;
import engine.logic.execution.ExecutionContextImpl;
import engine.logic.programData.architecture.ArchitectureType;
import engine.logic.programData.instruction.Instruction;
import engine.logic.programData.label.FixedLabel;
import engine.logic.programData.label.Label;
import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;
import engine.logic.programData.variable.VariableType;

import java.io.Serializable;
import java.util.*;

public class ProgramExecutorImpl implements ProgramExecutor, Serializable {

    private final Program program;
    private ExecutionContext context;
    private List<Long> inputsValues;
    private int runDegree = 0;
    private int totalCycles = 0;
    private ArchitectureType architectureTypeSelected;

    public ProgramExecutorImpl(Program program, ArchitectureType architectureTypeSelected) {
        this.program = program;
        this.architectureTypeSelected = architectureTypeSelected;
        this.context = new ExecutionContextImpl();
        this.inputsValues = new ArrayList<>();
    }

    @Override
    public void run(UserDTO userDTO, int runDegree, Long... inputs) {
        Instruction currentInstruction = program.getInstructionsList().getFirst();
        Instruction nextInstruction = null;
        Label nextLabel;

        inputsValues = List.of(inputs);
        context.initializeVariables(program, inputs);
        this.runDegree = runDegree;
        this.architectureTypeSelected = architectureTypeSelected;

        do {
                nextLabel = currentInstruction.execute(context, userDTO);

                // Cycles update:
                int currentInstructionCycles  = currentInstruction.getCycleOfInstruction();
                totalCycles += currentInstructionCycles ;
                userDTO.subtractFromCurrentCredits(currentInstructionCycles );

                if (nextLabel == FixedLabel.EMPTY) {
                    int indexOfNextInstruction = program.getInstructionsList().indexOf(currentInstruction) + 1;

                    // If there is more instructions, else Exit
                    if (indexOfNextInstruction < program.getInstructionsList().size()) {
                        nextInstruction = program.getInstructionsList().get(indexOfNextInstruction);
                    } else {
                        nextLabel = FixedLabel.EXIT;
                    }
                } else if (nextLabel != FixedLabel.EXIT) {
                        nextInstruction = program.getInstructionByLabel(nextLabel);
                }

                currentInstruction = nextInstruction;

            // TODO: to add the not finish run to the history
        } while(nextLabel != FixedLabel.EXIT);

        this.program.addCreditCostOfProgram(totalCycles);
        context.getVariableValue(Variable.RESULT);
    }

    @Override
    public Program getProgram() {
        return program;
    }

    @Override
    public long getVariableValue(Variable variable) {
        return context.getVariableValue(variable);
    }

    @Override
    public int getRunDegree() {
        return runDegree;
    }

    @Override
    public List<Long> getInputsValuesOfUser() {
        return inputsValues;
    }

    @Override
    public int getTotalCycles() {
        return this.totalCycles;
    }

    @Override
    public Map<String, Long> getVariablesToValuesSorted() {
        Map<String, Long> variablesToValuesSorted = new LinkedHashMap<>();

        variablesToValuesSorted.put(VariableType.RESULT.getVariableRepresentation(0), context.getVariableValue(Variable.RESULT));

        for (Variable v : program.getInputAndWorkVariablesSortedBySerial()) {
            variablesToValuesSorted.put(v.getRepresentation(), context.getVariableValue(v));
        }

        return variablesToValuesSorted;
    }

    @Override
    public ArchitectureType getArchitectureTypeSelected() {
        return architectureTypeSelected;
    }

    @Override
    public void setRunDegree(int runDegree) {
        this.runDegree = runDegree;
    }

    @Override
    public void setTotalCycles(int totalCycles) {
        this.totalCycles = totalCycles;
    }

    @Override
    public void setExecutionContext(ExecutionContext executionContext) {
        this.context = executionContext;
    }

    @Override
    public void setInputsValues(List<Long> inputsValues) {
        this.inputsValues = inputsValues;
    }
}