package instruction.basic;

import execution.ExecutionContext;
import instruction.*;
import label.Label;
import label.FixedLabel;
import variable.Variable;

public class IncreaseInstruction extends AbstractInstruction implements BasicInstruction {

    public IncreaseInstruction(Variable variable, Instruction origin, int instructionNumber) {
        super(InstructionData.INCREASE, InstructionType.BASIC ,variable, FixedLabel.EMPTY, origin, instructionNumber);
    }

    public IncreaseInstruction(Variable variable, Label label, Instruction origin, int instructionNumber) {
        super(InstructionData.INCREASE, InstructionType.BASIC, variable, label, origin,  instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getTargetVariable());

        context.updateVariable(getTargetVariable() ,variableValue + 1);

        return FixedLabel.EMPTY;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new IncreaseInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Instruction cloneInstruction(Variable targetVariable, Label label, Label referencesLabel, Instruction origin, int instructionNumber) {
        return new IncreaseInstruction(targetVariable, label, origin, instructionNumber);
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(variableRepresentation);
        command.append(" + 1");

        return command.toString();
    }
}
