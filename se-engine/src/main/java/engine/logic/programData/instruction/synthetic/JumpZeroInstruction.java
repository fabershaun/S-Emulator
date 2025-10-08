package engine.logic.programData.instruction.synthetic;

import dto.v3.UserDTO;
import engine.logic.execution.ExecutionContext;
import engine.logic.programData.instruction.*;
import engine.logic.programData.instruction.basic.JumpNotZeroInstruction;
import engine.logic.programData.instruction.basic.NoOpInstruction;
import engine.logic.programData.label.FixedLabel;
import engine.logic.programData.label.Label;
import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpZeroInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencesLabel;

    public JumpZeroInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_ZERO, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY, origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    public JumpZeroInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Label label, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_ZERO, InstructionType.SYNTHETIC, variable, label, origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), referencesLabel, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context, UserDTO userDTO) {
        long variableValue = context.getVariableValue(this.getTargetVariable());

        return variableValue == 0 ? this.referencesLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();

        String command = "IF " +
                variableRepresentation +
                " = 0 GOTO " +
                referencesLabel.getLabelRepresentation();

        return command;
    }

    @Override
    public Label getReferenceLabel() {
        return referencesLabel;
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int expandInstruction(int startNumber) {
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 =  super.getMainProgram().generateUniqueLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(),newLabel1, newLabel2, this, instructionNumber++));
        innerInstructions.add(new GotoLabelInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(), referencesLabel, this, instructionNumber++));
        innerInstructions.add(new NoOpInstruction(getMainProgram(), getProgramOfThisInstruction(), Variable.RESULT, newLabel2, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin, Program mainProgram) {
        setMainProgram(mainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());
        Label newReferenceLabel = labelMap.getOrDefault(this.getReferenceLabel(), this.getReferenceLabel());

        return new JumpZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, newReferenceLabel, origin, newInstructionNumber);
    }
}
