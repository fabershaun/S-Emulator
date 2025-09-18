package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.IncreaseInstruction;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstantAssignmentInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final int MAX_DEGREE = 2;
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final long constantValue;

    public ConstantAssignmentInstruction(Program programOfThisInstruction, Variable targetVariable, long constantValue, Instruction origin, int instructionNumber) {
        super(programOfThisInstruction, InstructionData.CONSTANT_ASSIGNMENT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.constantValue = constantValue;
    }

    public ConstantAssignmentInstruction(Program programOfThisInstruction, Variable targetVariable, Label label, long constantValue, Instruction origin, int instructionNumber) {
        super(programOfThisInstruction, InstructionData.CONSTANT_ASSIGNMENT, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.constantValue = constantValue;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int  instructionNumber) {
        return new ConstantAssignmentInstruction(getProgramOfThisInstruction(), getTargetVariable(), getLabel(), constantValue, getOriginalInstruction(), instructionNumber);
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
        return MAX_DEGREE;
    }


    @Override
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new ZeroVariableInstruction(getProgramOfThisInstruction(), super.getTargetVariable(), newLabel1, this, instructionNumber++));

        for(int i = 0 ; i < constantValue ; i++) {
            innerInstructions.add(new IncreaseInstruction(getProgramOfThisInstruction(), super.getTargetVariable(), this,  instructionNumber++));
        }

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new ConstantAssignmentInstruction(getProgramOfThisInstruction(), newTargetVariable, newLabel, this.constantValue, this.getOriginalInstruction(), newInstructionNumber);
    }
}
