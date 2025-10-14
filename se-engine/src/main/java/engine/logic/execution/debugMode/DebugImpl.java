package engine.logic.execution.debugMode;

import dto.v2.DebugDTO;
import dto.v2.ProgramDTO;
import dto.v2.ProgramExecutorDTO;
import dto.v3.UserDTO;
import engine.EngineImpl;
import engine.logic.execution.ExecutionContext;
import engine.logic.execution.ExecutionContextImpl;
import engine.logic.execution.runMode.ProgramExecutor;
import engine.logic.execution.runMode.ProgramExecutorImpl;
import engine.logic.programData.architecture.ArchitectureType;
import engine.logic.programData.instruction.Instruction;
import engine.logic.programData.label.FixedLabel;
import engine.logic.programData.label.Label;
import engine.logic.programData.program.Program;
import engine.user.UserLogic;

import java.util.ArrayList;
import java.util.List;

import static engine.EngineImpl.buildProgramDTO;

public class DebugImpl implements Debug {

    private final String uploaderName;
    private final UserDTO userDTO;
    private final ProgramExecutor programExecutor;
    private final ProgramExecutor initializeProgramExecutor;
    private final Program program;

    private final ExecutionContext context = new ExecutionContextImpl();
    private final List<Instruction> instructions;

    private String targetVariable;

    private int currentInstructionIndex = 0;
    private int nextInstructionIndex = 0;
    private int currentCycles = 0;
    private final List<DebugDTO> stepsHistory = new ArrayList<>();
    private int historyPointer = -1;
    private boolean justStoppedOnBreakpoint = false;

    public DebugImpl(Program program, ArchitectureType architectureTypeSelected, int degree, List<Long> inputs, String uploaderName, UserDTO userDTO) { // TODO: why need both uploaderName, and userDTO
        this.program = program;
        this.uploaderName = uploaderName;
        this.userDTO = userDTO;
        this.instructions = program.getInstructionsList();
        context.initializeVariables(program, inputs.toArray(new Long[0]));
        ExecutionContext initializeContext = new ExecutionContextImpl();
        initializeContext.initializeVariables(program, inputs.toArray(new Long[0]));

        this.programExecutor = new ProgramExecutorImpl(program, architectureTypeSelected);
        this.programExecutor.setInputsValues(inputs);
        this.programExecutor.setRunDegree(degree);

        this.initializeProgramExecutor = new ProgramExecutorImpl(program, architectureTypeSelected);
        this.initializeProgramExecutor.setInputsValues(inputs);
        this.initializeProgramExecutor.setRunDegree(degree);
        this.initializeProgramExecutor.setExecutionContext(initializeContext);
        this.initializeProgramExecutor.setTotalCycles(currentCycles);
    }

    private boolean checkAndStopAtBreakpoint(List<Boolean> breakPoints) {
        if (currentInstructionIndex < breakPoints.size() && breakPoints.get(currentInstructionIndex)) {
            DebugDTO snapshot;

            // Case: very first instruction, before any step was executed
            if (currentInstructionIndex == 0 && historyPointer < 0) {
                ProgramExecutorDTO initializedExecutorDTO = buildProgramExecutorDTO(initializeProgramExecutor);

                snapshot = new DebugDTO(
                        this.programExecutor.getProgram().getName(),
                        this.programExecutor.getArchitectureTypeSelected().getArchitectureRepresentation(),
                        0, // current instruction index
                        0, // next instruction index (still at start)
                        hasMoreInstructions(),
                        null, // no target variable yet
                        initializedExecutorDTO.getDegree(),
                        initializedExecutorDTO.getResult(),
                        initializedExecutorDTO.getTotalCycles(),
                        initializedExecutorDTO.getVariablesToValuesSorted()
                );
            } else {
                // Normal case, after at least one step
                snapshot = buildDebugDTO();
            }

            stepsHistory.add(snapshot);
            historyPointer = stepsHistory.size() - 1;
            justStoppedOnBreakpoint = true;
            return true;
        }
        return false;
    }


    @Override
    public DebugDTO resume(List<Boolean> breakPoints) throws InterruptedException {
        // If resume is called before any step has been executed
        if (historyPointer < 0 && hasMoreInstructions()) {
            if (checkAndStopAtBreakpoint(breakPoints)) {
                return stepsHistory.get(historyPointer);
            }
            stepOver();   // create the first snapshot
        }

        if (justStoppedOnBreakpoint) {
            justStoppedOnBreakpoint = false;
            stepOver();
        }

        while (hasMoreInstructions()) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("Debug cancelled at instruction " + currentInstructionIndex);
                throw new InterruptedException("In DebugImpl - resume(): currentThread cancelled by user");
            }

            if (checkAndStopAtBreakpoint(breakPoints)) {
                return stepsHistory.get(historyPointer);
            }

            stepOverWithoutSavingHistory();
        }

        // finished program (no more instructions)
        DebugDTO finalSnapshot = buildDebugDTO();
        stepsHistory.add(finalSnapshot);
        historyPointer = stepsHistory.size() - 1;

        return finalSnapshot;
    }


    // Note: ** NOT touch history here **
    private void stepOverWithoutSavingHistory() {
        if (currentInstructionIndex >= instructions.size()) {
            return; // already out of range
        }

        Instruction currentInstruction = instructions.get(currentInstructionIndex);

        // Execute the instruction on the current context
        Label nextInstructionLabel = currentInstruction.execute(context, userDTO);

        // Update cycles and credits
        updateCyclesAndCredits(currentInstruction);

        // Update target variable if exists
        if (currentInstruction.getTargetVariable() != null) {
            targetVariable = currentInstruction.getTargetVariable().getRepresentation();
        } else {
            targetVariable = null;
        }

        // Update programExecutor (context + cycles)
        updateProgramExecutorData();

        // Update index of the next instruction
        updateNextInstructionIndexToNextIndex(nextInstructionLabel);
        currentInstructionIndex = nextInstructionIndex;

    }

    @Override
    public DebugDTO stepOver() {

        if(hasMoreInstructions()) {
            if(historyPointer > stepsHistory.size() - 1) {
                throw new IllegalArgumentException("In DebugImpl: historyPointer is out of range. (historyPointer and currentInstructionIndex aren't synchronized");
            }
            else if(historyPointer == stepsHistory.size() - 1) {
                Instruction currentInstruction = instructions.get(currentInstructionIndex);
                Label nextInstructionLabel = currentInstruction.execute(context, userDTO);
                targetVariable = currentInstruction.getTargetVariable().getRepresentation();

                updateCyclesAndCredits(currentInstruction);
                updateProgramExecutorData();
                updateNextInstructionIndexToNextIndex(nextInstructionLabel);

                currentInstructionIndex = nextInstructionIndex;
                stepsHistory.add(buildDebugDTO());
                ++historyPointer;
            }
            else {    // When historyPointer is less than the list size
                historyPointer++;
                updateCreditsOnly();
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
            // Charge for stepping back over the very first step (totalCycles at step 0 minus first step)
            if (!stepsHistory.isEmpty()) {
                int firstStepCycles = stepsHistory.getFirst().getTotalCycles();
                UserLogic.subtractCredits(userDTO, firstStepCycles); // consume credits for the action
            }

            currentInstructionIndex = 0;
            nextInstructionIndex = 0;

            ProgramExecutorDTO initializedProgramExecutor = buildProgramExecutorDTO(initializeProgramExecutor);

            return new DebugDTO(
                    this.programExecutor.getProgram().getName(),
                    this.programExecutor.getArchitectureTypeSelected().getArchitectureRepresentation(),
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

        // Case: historyPointer is still inside the valid range
        if (historyPointer < stepsHistory.size()) {
            // We already have a snapshot for this step
            updateCreditsOnly();
            DebugDTO dto = stepsHistory.get(historyPointer);
            currentInstructionIndex = dto.getCurrentInstructionNumber();
            nextInstructionIndex = dto.getNextInstructionNumber();
            return dto;
        } else {
            // We got here after a resume run that skipped saving some states
            DebugDTO snapshot = buildDebugDTO();
            stepsHistory.add(snapshot);
            historyPointer = stepsHistory.size() - 1;

            updateCreditsOnly();
            currentInstructionIndex = snapshot.getCurrentInstructionNumber();
            nextInstructionIndex = snapshot.getNextInstructionNumber();
            return snapshot;
        }
    }

    @Override
    public DebugDTO stop() {
        return stepsHistory.get(historyPointer);
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
            throw new IllegalArgumentException("In DebugImpl: Instruction number: "
                    + currentInstructionIndex + ". Message: " + ev.getMessage());
        }
    }

    @Override
    public String getUploaderName() {
        return uploaderName;
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

    private DebugDTO buildDebugDTO() {
        ProgramExecutorDTO executorDTO = buildProgramExecutorDTO(this.programExecutor);

        return new DebugDTO(
                this.programExecutor.getProgram().getName(),
                this.programExecutor.getArchitectureTypeSelected().getArchitectureRepresentation(),
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

    private void updateCreditsOnly() {
        if (historyPointer <= 0) {
            return; // No previous level to compare to
        }

        int totalCyclesInCurrentInstruction = stepsHistory.get(historyPointer).getTotalCycles();
        int totalCyclesInPreviousInstruction = stepsHistory.get(historyPointer - 1).getTotalCycles();
        int currentInstructionCycles = totalCyclesInCurrentInstruction - totalCyclesInPreviousInstruction;

        UserLogic.subtractCredits(userDTO, currentInstructionCycles);
        programExecutor.getProgram().addCreditCost(currentInstructionCycles);
    }

    private void updateCyclesAndCredits(Instruction currentInstruction) {
        int currentInstructionCycles = currentInstruction.getCycleOfInstruction();
        currentCycles += currentInstructionCycles;

        UserLogic.subtractCredits(userDTO, currentInstructionCycles);
        programExecutor.getProgram().addCreditCost(currentInstructionCycles);
    }

    private void updateProgramExecutorData() {
        this.programExecutor.setExecutionContext(context);
        this.programExecutor.setTotalCycles(currentCycles);
    }

    private String getTargetVariableOfCurrentInstruction() {    // If: don't have target variable -> return null
        return targetVariable;
    }
}
