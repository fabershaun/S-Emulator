package engine.logic.instruction.basic;

import engine.logic.execution.ExecutionContext;
import engine.logic.instruction.AbstractInstruction;
import engine.logic.instruction.Instruction;
import engine.logic.instruction.InstructionData;
import engine.logic.instruction.InstructionType;
import engine.logic.label.FixedLabel;
import engine.logic.label.Label;
import engine.logic.program.Program;
import engine.logic.variable.Variable;

import java.util.Map;

public class NoOpInstruction extends AbstractInstruction {

    public NoOpInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.NO_OP, InstructionType.BASIC ,variable, FixedLabel.EMPTY, origin ,instructionNumber);
    }

    public NoOpInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Label label, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.NO_OP, InstructionType.BASIC, variable, label,  origin, instructionNumber);
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new NoOpInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();

        String command = variableRepresentation +
                " <- " +
                variableRepresentation;

        return command;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin, Program mainProgram) {
        setMainProgram(mainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new NoOpInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, origin, newInstructionNumber);
    }
}
