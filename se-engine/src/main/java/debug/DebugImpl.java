package debug;

import dto.DebugDTO;
import dto.InstructionDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.EngineImpl;
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

public class DebugImpl implements Debug {

    private final ProgramExecutor programExecutor;
    private final ProgramExecutor initializeProgramExecutor;
    private final Program program;

    private final ExecutionContext context = new ExecutionContextImpl();
    private final ExecutionContext initializeContext = new ExecutionContextImpl();
    private final List<Instruction> instructions;
    private final List<Long> inputs;

    private String targetVariable;

    private int currentInstructionIndex = 0;
    private int nextInstructionIndex = 0;
    private int currentCycles = 0;
    private final List<DebugDTO> stepsHistory = new ArrayList<>();
    private int historyPointer = -1;
    private boolean justStoppedOnBreakpoint = false;

    public DebugImpl(Program program, int degree, List<Long> inputs) {
        this.program = program;
        this.instructions = program.getInstructionsList();
        this.inputs = inputs;
        context.initializeVariables(program, inputs.toArray(new Long[0]));
        initializeContext.initializeVariables(program, inputs.toArray(new Long[0]));

        this.programExecutor = new ProgramExecutorImpl(program);
        this.programExecutor.setInputsValues(inputs);
        this.programExecutor.setRunDegree(degree);

        this.initializeProgramExecutor = new ProgramExecutorImpl(program);
        this.initializeProgramExecutor.setInputsValues(inputs);
        this.initializeProgramExecutor.setRunDegree(degree);
        this.initializeProgramExecutor.setExecutionContext(initializeContext);
        this.initializeProgramExecutor.setTotalCycles(currentCycles);
    }

    @Override
    public DebugDTO resume(List<Boolean> breakPoints) {

        if (justStoppedOnBreakpoint) {
            justStoppedOnBreakpoint = false;
            stepOver();
        }

        while (hasMoreInstructions()) {
            int indexBP = currentInstructionIndex;

            if (indexBP >= 0 && indexBP < breakPoints.size() && breakPoints.get(indexBP)) {   // Break point is ON in this instruction line
                justStoppedOnBreakpoint = true;
                return stop();  // return the value before enter the line
            }

            stepOver();
        }

        return stop();
    }

    @Override
    public DebugDTO stepOver() {

        if(hasMoreInstructions()) {
            if(historyPointer > stepsHistory.size() - 1) {
                throw new IllegalArgumentException("In DebugImpl: historyPointer is out of range. (historyPointer and currentInstructionIndex aren't synchronized");
            } else if(historyPointer == stepsHistory.size() - 1) {
                Instruction currentInstruction = instructions.get(currentInstructionIndex);
                Label nextInstructionLabel = currentInstruction.execute(context);
                currentCycles +=  currentInstruction.getCycleOfInstruction();
                targetVariable = currentInstruction.getTargetVariable().getRepresentation();

                updateProgramExecutorData();
                updateNextInstructionIndexToNextIndex(nextInstructionLabel);
                currentInstructionIndex = nextInstructionIndex;

                stepsHistory.add(buildDebugDTO());
                ++historyPointer;
            } else {    // When historyPointer is less then the list size
                historyPointer++;
                DebugDTO dto = stepsHistory.get(historyPointer);
                currentInstructionIndex = dto.getCurrentInstructionNumber();
                nextInstructionIndex = dto.getNextInstructionNumber();
            }
        }

        return stepsHistory.get(historyPointer);
    }

    private void updateNextInstructionIndexToNextIndex(Label nextInstructionLabel) {
        if (nextInstructionLabel.equals(FixedLabel.EMPTY)) {
            nextInstructionIndex++;  // Step Over
        } else if (nextInstructionLabel.equals(FixedLabel.EXIT)) {
            nextInstructionIndex = instructions.size(); // Finish
        } else {
            nextInstructionIndex = program.getLabelToInstruction().get(nextInstructionLabel).getInstructionNumber() - 1; // Jump to instruction by label   // Instructions start counting from 1
        }
    }

    @Override
    public DebugDTO stepBack() {
        historyPointer--;

        if (historyPointer < 0) {    // Before start
            currentInstructionIndex = 0;
            nextInstructionIndex = 0;

            ProgramExecutorDTO initializedProgramExecutor = buildProgramExecutorDTO(initializeProgramExecutor);

            return new DebugDTO(
                    this.programExecutor.getProgram().getName(),
                    getCurrentInstructionIndex(),
                    getNextInstructionIndex(),
                    hasMoreInstructions(),
                    null,
                    initializedProgramExecutor.getDegree(),
                    initializedProgramExecutor.getResult(),
                    initializedProgramExecutor.getTotalCycles(),
                    initializedProgramExecutor.getVariablesToValuesSorted()
            );
        }

        DebugDTO dto = stepsHistory.get(historyPointer);
        currentInstructionIndex = dto.getCurrentInstructionNumber();
        nextInstructionIndex = dto.getNextInstructionNumber();
        return dto;
    }

    @Override
    public DebugDTO stop() {
        return stepsHistory.get(historyPointer);
    }

    private DebugDTO buildDebugDTO() {
        ProgramExecutorDTO executorDTO = buildProgramExecutorDTO(this.programExecutor);

        return new DebugDTO(
                this.programExecutor.getProgram().getName(),
                getCurrentInstructionIndex(),
                getNextInstructionIndex(),
                hasMoreInstructions(),
                getTargetVariableOfCurrentInstruction(),
                executorDTO.getDegree(),
                executorDTO.getResult(),
                executorDTO.getTotalCycles(),
                executorDTO.getVariablesToValuesSorted()
        );
    }

    @Override
    public boolean hasMoreInstructions() {
        if (currentInstructionIndex >= instructions.size()) {
            return false;
        }

        return !instructions.get(currentInstructionIndex).getLabel().equals(FixedLabel.EXIT);   // If label is not 'EXIT' than true
    }

    @Override
    public ProgramExecutorDTO buildProgramExecutorDTO(ProgramExecutor programExecutor) {
        try {
            ProgramDTO programDTO = buildProgramDTO(this.program);
            return EngineImpl.buildProgramExecutorDTO(programDTO, programExecutor);
        } catch (Exception ev) {
            throw new IllegalArgumentException("In DebugImpl: Instruction number: " + currentInstructionIndex + ". Message: " + ev.getMessage());
        }
    }

    @Override
    public ProgramExecutor getDebugProgramExecutor() {
        return programExecutor;
    }

    @Override
    public int getCurrentInstructionIndex() {
        return currentInstructionIndex; // Return the index after the update
    }

    @Override
    public int getNextInstructionIndex() {
        return nextInstructionIndex; // Return the index after the update
    }

    private void updateProgramExecutorData() {
        this.programExecutor.setExecutionContext(context);
        this.programExecutor.setTotalCycles(currentCycles);
    }

    private String getTargetVariableOfCurrentInstruction() {    // If don't have target variable return null
        return targetVariable;
    }
}
