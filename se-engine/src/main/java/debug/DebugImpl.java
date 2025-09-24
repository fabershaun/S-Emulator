package debug;

import dto.DebugDTO;
import execution.ExecutionContext;
import execution.ExecutionContextImpl;
import instruction.Instruction;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;
import variable.VariableType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DebugImpl implements Debug {
    private final Program program;
    private final ExecutionContext context = new ExecutionContextImpl();
    private final List<Instruction> instructions;

    private Label nextInstructionLabel = FixedLabel.EMPTY;
    private int currentInstructionIndex = -1;
    private int currentCycles = 0;
    private final List<DebugDTO> stepsHistory = new ArrayList<>();
    private int historyPointer = -1;

    public DebugImpl(Program program, List<Long> inputs) {
        this.program = program;
        this.instructions = program.getInstructionsList();
        context.initializeVariables(program, inputs.toArray(new Long[0]));
    }


    @Override
    public DebugDTO resume() {
        while (hasMoreInstructions()) {
            stepOver();
        }

        return stepsHistory.get(historyPointer);
    }

    @Override
    public DebugDTO stepOver() {
        updateCurrentInstructionIndex();

        if(hasMoreInstructions()) {
            if(historyPointer == stepsHistory.size() - 1) {
                Instruction currentInstruction = instructions.get(currentInstructionIndex);
                nextInstructionLabel = currentInstruction.execute(context);
                currentCycles +=  currentInstruction.getCycleOfInstruction();
                stepsHistory.add(buildDebugDTO());
                historyPointer++;
            } else {
                return stepsHistory.get(++historyPointer);
            }
        }

        return stepsHistory.get(historyPointer);
    }

    private void updateCurrentInstructionIndex() {
        if (nextInstructionLabel.equals(FixedLabel.EMPTY)) {
            currentInstructionIndex++;  // Step Over
        } else if (nextInstructionLabel.equals(FixedLabel.EXIT)) {
            currentInstructionIndex = instructions.size(); // Finish
        } else {
            currentInstructionIndex = program.getLabelToInstruction().get(nextInstructionLabel).getInstructionNumber(); // Jump to instruction by label
        }
    }

    @Override
    public DebugDTO stepBack() {
        --historyPointer;
        if (historyPointer < 0) {
            throw new IllegalArgumentException("Step back in debug failed. Cannot step back before the first instruction");
        }

        currentInstructionIndex = stepsHistory.get(historyPointer).getInstructionNumber();
        return stepsHistory.get(historyPointer);
    }

    private DebugDTO buildDebugDTO() {
        return new DebugDTO(
                getVariablesToValuesSorted(),
                getResult(),
                getCurrentInstructionIndex(),
                getCurrentCycles(),
                hasMoreInstructions()
        );
    }

    @Override
    public boolean hasMoreInstructions() {
        if (currentInstructionIndex >= instructions.size()) {
            return false;
        } else {
            return !instructions.get(currentInstructionIndex).getLabel().equals(FixedLabel.EXIT);   // If label is not 'EXIT' than true
        }
    }

    @Override
    public Map<String, Long> getVariablesToValuesSorted() {
        Map<String, Long> variablesToValuesSorted = new LinkedHashMap<>();

        variablesToValuesSorted.put(VariableType.RESULT.getVariableRepresentation(0), context.getVariableValue(Variable.RESULT));

        for (Variable variable : program.getInputAndWorkVariablesSortedBySerial()) {
            variablesToValuesSorted.put(variable.getRepresentation(), context.getVariableValue(variable));
        }

        return variablesToValuesSorted;
    }

    @Override
    public long getResult() {
        return context.getVariableValue(Variable.RESULT);
    }

    @Override
    public int getCurrentInstructionIndex() {
        return currentInstructionIndex;
    }

    @Override
    public int getCurrentCycles() {
        return currentCycles;
    }
}
