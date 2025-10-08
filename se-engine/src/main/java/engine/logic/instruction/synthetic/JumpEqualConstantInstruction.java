package engine.logic.instruction.synthetic;

import engine.logic.execution.ExecutionContext;
import engine.logic.instruction.*;
import instruction.*;
import engine.logic.instruction.basic.DecreaseInstruction;
import engine.logic.instruction.basic.JumpNotZeroInstruction;
import engine.logic.instruction.basic.NoOpInstruction;
import engine.logic.label.FixedLabel;
import engine.logic.label.Label;
import engine.logic.program.Program;
import engine.logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualConstantInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencelabel;
    private final long constantValue;

    public JumpEqualConstantInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, long constantValue, Label referencelabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.constantValue = constantValue;
        this.referencelabel = referencelabel;

    }

    public JumpEqualConstantInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Label label, long constantValue, Label referencelabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.constantValue = constantValue;
        this.referencelabel = referencelabel;
    }

    @Override
    public long getConstantValue() {
        return this.constantValue;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpEqualConstantInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), constantValue, referencelabel, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getTargetVariable());

        return (variableValue == constantValue) ? referencelabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();

        String command = "IF " +
                variableRepresentation +
                " = " +
                constantValue +
                " GOTO " +
                referencelabel.getLabelRepresentation();

        return command;
    }

    @Override
    public Label getReferenceLabel() {
        return referencelabel;
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int expandInstruction(int startNumber) {
        Variable workVariable1 = super.getMainProgram().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 = super.getMainProgram().generateUniqueLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new AssignmentInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel1 ,super.getTargetVariable(), this, instructionNumber++));

        for(int i = 0 ; i < constantValue ; i++) {
            innerInstructions.add(new JumpZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel2, this, instructionNumber++));
            innerInstructions.add(new DecreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, this, instructionNumber++));
        }

        innerInstructions.add(new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel2, this, instructionNumber++));
        innerInstructions.add(new GotoLabelInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(), referencelabel, this, instructionNumber++));
        innerInstructions.add(new NoOpInstruction(getMainProgram(), getProgramOfThisInstruction(), Variable.RESULT, newLabel2, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin, Program mainProgram) {
        setMainProgram(mainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());
        Label newReferenceLabel = labelMap.getOrDefault(this.getReferenceLabel(), this.getReferenceLabel());

        return new JumpEqualConstantInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, this.constantValue, newReferenceLabel, origin, newInstructionNumber);
    }
}
