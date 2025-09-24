package debug;

import dto.DebugDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import execution.ExecutionContext;
import execution.ExecutionContextImpl;
import execution.ProgramExecutor;
import execution.ProgramExecutorImpl;
import instruction.Instruction;
import label.FixedLabel;
import label.Label;
import program.Program;

import java.util.ArrayList;
import java.util.List;

import static engine.EngineImpl.buildProgramDTO;
import static engine.EngineImpl.buildProgramExecutorDTO;

public class DebugImpl implements Debug {

    private final ProgramExecutor programExecutor;
    private final Program program;
    private final ExecutionContext context = new ExecutionContextImpl();
    private final List<Instruction> instructions;

    private Label nextInstructionLabel = FixedLabel.EMPTY;
    private int currentInstructionIndex = -1;
    private int currentCycles = 0;
    private final List<DebugDTO> stepsHistory = new ArrayList<>();
    private int historyPointer = -1;

    public DebugImpl(Program program, int degree, List<Long> inputs) {
        this.program = program;
        this.instructions = program.getInstructionsList();
        context.initializeVariables(program, inputs.toArray(new Long[0]));

        this.programExecutor = new ProgramExecutorImpl(program);
        this.programExecutor.setInputsValues(inputs);
        this.programExecutor.setRunDegree(degree);
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
                updateProgramExecutorData();
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
            currentInstructionIndex = program.getLabelToInstruction().get(nextInstructionLabel).getInstructionNumber() - 1; // Jump to instruction by label   // Instructions start counting from 1
        }
    }

    @Override
    public DebugDTO stepBack() {
        historyPointer--;

        if (historyPointer < 0) {    // Before start
            currentInstructionIndex = -1;
            currentCycles = 0;
            return new DebugDTO(getDebugProgramExecutorDTO(), getCurrentInstructionIndex(), hasMoreInstructions());
        }

        currentInstructionIndex = stepsHistory.get(historyPointer).getInstructionNumber();
        return stepsHistory.get(historyPointer);
    }

    @Override
    public DebugDTO stop() {
        return stepsHistory.get(historyPointer);
    }

    private DebugDTO buildDebugDTO() {
        return new DebugDTO(
                getDebugProgramExecutorDTO(),
                getCurrentInstructionIndex(),
                hasMoreInstructionsNotIncludingLast()
        );
    }

    private boolean hasMoreInstructions() {
        if (currentInstructionIndex < 0) {  // Didnt start yet (value = -1) -> return true
            return !instructions.isEmpty();
        }

        if (currentInstructionIndex >= instructions.size()) {
            return false;
        }

        return !instructions.get(currentInstructionIndex).getLabel().equals(FixedLabel.EXIT);   // If label is not 'EXIT' than true
    }

    @Override
    public boolean hasMoreInstructionsNotIncludingLast() {
        return hasMoreInstructions() && currentInstructionIndex < instructions.size() - 1;
    }

    @Override
    public ProgramExecutorDTO getDebugProgramExecutorDTO() {
        ProgramDTO programDTO = buildProgramDTO(this.program);
        return buildProgramExecutorDTO(programDTO, this.programExecutor);
    }

    @Override
    public ProgramExecutor getDebugProgramExecutor() {
        return programExecutor;
    }

    @Override
    public int getCurrentInstructionIndex() {
        return currentInstructionIndex;
    }

    private void updateProgramExecutorData() {
        this.programExecutor.setExecutionContext(context);
        this.programExecutor.setTotalCycles(currentCycles);
    }
}
