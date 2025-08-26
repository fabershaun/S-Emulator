package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.IncreaseInstruction;
import label.FixedLabel;
import label.Label;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class ConstantAssignmentInstruction extends AbstractInstruction implements SyntheticInstruction, ConstantInstructions {

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final long constantValue;

    public ConstantAssignmentInstruction(Variable targetVariable, long constantValue, Instruction origin, int instructionNumber) {
        super(InstructionData.CONSTANT_ASSIGNMENT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.constantValue = constantValue;
    }

    public ConstantAssignmentInstruction(Variable targetVariable, Label label, long constantValue, Instruction origin, int instructionNumber) {
        super(InstructionData.CONSTANT_ASSIGNMENT, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.constantValue = constantValue;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int  instructionNumber) {
        return new ConstantAssignmentInstruction(getTargetVariable(), getLabel(), constantValue, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Instruction CloneInstruction(Variable targetVariable, Label label, Variable sourceVariable, long constantValue, Label referencesLabel, Instruction origin, int instructionNumber) {
        return new ConstantAssignmentInstruction(targetVariable, label, constantValue, origin, instructionNumber);
    }

    @Override
    public long getConstantValue() {
        return constantValue;
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getTargetVariable(), constantValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(constantValue);

        return command.toString();
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int getMaxDegree() {
        int maxDegree = 2;
        return maxDegree;
    }


    @Override
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new ZeroVariableInstruction(super.getTargetVariable(), newLabel1, this, instructionNumber++));

        for(int i = 0 ; i < constantValue ; i++) {
            innerInstructions.add(new IncreaseInstruction(super.getTargetVariable(), this,  instructionNumber++));
        }

        return instructionNumber;
    }

}
