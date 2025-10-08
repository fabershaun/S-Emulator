package engine.logic.programData.instruction.basic;

import dto.v3.UserDTO;
import engine.logic.execution.ExecutionContext;
import engine.logic.programData.instruction.*;
import engine.logic.programData.label.FixedLabel;
import engine.logic.programData.label.Label;
import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;

import java.util.Map;

public class JumpNotZeroInstruction extends AbstractInstruction implements LabelReferencesInstruction {
    private final Label referencesLabel;

    public JumpNotZeroInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_NOT_ZERO, InstructionType.BASIC ,variable, FixedLabel.EMPTY, origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    public JumpNotZeroInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Label label, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_NOT_ZERO, InstructionType.BASIC, variable, label,  origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), referencesLabel, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context, UserDTO userDTO) {
        long variableValue = context.getVariableValue(this.getTargetVariable());

        return variableValue != 0 ? this.referencesLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();

        String command = "IF " +
                variableRepresentation +
                " != 0 GOTO " +
                referencesLabel.getLabelRepresentation();

        return command;
    }

    @Override
    public Label getReferenceLabel() {
        return referencesLabel;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin, Program mainProgram) {
        setMainProgram(mainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());
        Label newReferenceLabel = labelMap.getOrDefault(this.getReferenceLabel(), this.getReferenceLabel());

        return new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, newReferenceLabel, origin, newInstructionNumber);
    }
}
