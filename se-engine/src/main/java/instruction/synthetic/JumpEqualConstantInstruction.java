package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.DecreaseInstruction;
import instruction.basic.JumpNotZeroInstruction;
import instruction.basic.NoOpInstruction;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualConstantInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {
    private final int MAX_DEGREE = 3;
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencelabel;
    private final long constantValue;

    public JumpEqualConstantInstruction(Program programOfThisInstruction, Variable targetVariable, long constantValue, Label referencelabel, Instruction origin, int instructionNumber) {
        super(programOfThisInstruction, InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.constantValue = constantValue;
        this.referencelabel = referencelabel;

    }

    public JumpEqualConstantInstruction(Program programOfThisInstruction, Variable targetVariable, Label label, long constantValue, Label referencelabel, Instruction origin, int instructionNumber) {
        super(programOfThisInstruction, InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.constantValue = constantValue;
        this.referencelabel = referencelabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpEqualConstantInstruction(getProgramOfThisInstruction(), getTargetVariable(), getLabel(), constantValue, referencelabel, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getTargetVariable());

        return (variableValue == constantValue) ? referencelabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(variableRepresentation);
        command.append(" = ");
        command.append(constantValue);
        command.append(" GOTO ");
        command.append(referencelabel.getLabelRepresentation());

        return command.toString();
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
    public int getMaxDegree() {
        return MAX_DEGREE;
    }

    @Override
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 = super.getProgramOfThisInstruction().generateUniqueLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new AssignmentInstruction(getProgramOfThisInstruction(), workVariable1, newLabel1 ,super.getTargetVariable(), this, instructionNumber++));

        for(int i = 0 ; i < constantValue ; i++) {
            innerInstructions.add(new JumpZeroInstruction(getProgramOfThisInstruction(), workVariable1, newLabel2, this, instructionNumber++));
            innerInstructions.add(new DecreaseInstruction(getProgramOfThisInstruction(), workVariable1, this, instructionNumber++));
        }

        innerInstructions.add(new JumpNotZeroInstruction(getProgramOfThisInstruction(), workVariable1, newLabel2, this, instructionNumber++));
        innerInstructions.add(new GotoLabelInstruction(getProgramOfThisInstruction(), super.getTargetVariable(), referencelabel, this, instructionNumber++));
        innerInstructions.add(new NoOpInstruction(getProgramOfThisInstruction(), Variable.RESULT, newLabel2, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());
        Label newReferenceLabel = labelMap.getOrDefault(this.getReferenceLabel(), this.getReferenceLabel());

        return new JumpEqualConstantInstruction(getProgramOfThisInstruction(), newTargetVariable, newLabel, this.constantValue, newReferenceLabel, this.getOriginalInstruction(), newInstructionNumber);
    }
}
