package engine.logic.programData.instruction.synthetic;

import engine.logic.execution.ExecutionContext;
import engine.logic.programData.instruction.*;
import engine.logic.programData.instruction.basic.DecreaseInstruction;
import engine.logic.programData.instruction.basic.NoOpInstruction;
import engine.logic.programData.label.FixedLabel;
import engine.logic.programData.label.Label;
import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualVariableInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencesLabel;
    private final Variable sourceVariable;

    public JumpEqualVariableInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Variable sourceVariable, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_EQUAL_VARIABLE, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.sourceVariable = sourceVariable;
        this.referencesLabel = referencesLabel;
    }

    public JumpEqualVariableInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Label label, Variable sourceVariable, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_EQUAL_VARIABLE, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.sourceVariable = sourceVariable;
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpEqualVariableInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), sourceVariable, referencesLabel, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long targetVariableValue = context.getVariableValue(getTargetVariable());
        long sourceVariableValue = context.getVariableValue(sourceVariable);

        return (targetVariableValue == sourceVariableValue) ? referencesLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String sourceVariableRepresentation = sourceVariable.getRepresentation();

        String command = "IF " +
                targetVariableRepresentation +
                " = " +
                sourceVariableRepresentation +
                " GOTO " +
                referencesLabel.getLabelRepresentation();

        return command;
    }

    @Override
    public Variable getSourceVariable() {
        return sourceVariable;
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
        int instructionNumber = startNumber;
        Variable workVariable1 = super.getMainProgram().generateUniqueVariable();
        Variable workVariable2 = super.getMainProgram().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 = super.getMainProgram().generateUniqueLabel();
        Label newLabel3 = super.getMainProgram().generateUniqueLabel();
        Label newLabel4 = super.getMainProgram().generateUniqueLabel();

        innerInstructions.add(new AssignmentInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel1, super.getTargetVariable(), this, instructionNumber++));
        innerInstructions.add(new AssignmentInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable2,sourceVariable, this, instructionNumber++));

        innerInstructions.add(new JumpZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel3, newLabel4, this, instructionNumber++));
        innerInstructions.add(new JumpZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable2, newLabel2, this, instructionNumber++));
        innerInstructions.add(new DecreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, this, instructionNumber++));
        innerInstructions.add(new DecreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable2, this, instructionNumber++));
        innerInstructions.add(new GotoLabelInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel3, this, instructionNumber++));
        innerInstructions.add(new JumpZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable2, newLabel4, referencesLabel, this, instructionNumber++));
        innerInstructions.add(new NoOpInstruction(getMainProgram(), getProgramOfThisInstruction(), Variable.RESULT, newLabel2, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin, Program mainProgram) {
        setMainProgram(mainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Variable newSourceVariable = variableMap.getOrDefault(this.getSourceVariable(), this.getSourceVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());
        Label newReferenceLabel = labelMap.getOrDefault(this.getReferenceLabel(), this.getReferenceLabel());

        return new JumpEqualVariableInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, newSourceVariable, newReferenceLabel, origin, newInstructionNumber);
    }
}
