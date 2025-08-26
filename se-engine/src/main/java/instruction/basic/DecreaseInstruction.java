package instruction.basic;

import execution.ExecutionContext;
import instruction.*;
import label.Label;
import label.FixedLabel;
import variable.Variable;

public class DecreaseInstruction extends AbstractInstruction implements BasicInstruction {

    public DecreaseInstruction(Variable variable, Instruction origin, int instructionNumber) {
        super(InstructionData.DECREASE, InstructionType.BASIC, variable, FixedLabel.EMPTY, origin, instructionNumber);
    }

    public DecreaseInstruction(Variable variable, Label label, Instruction origin, int instructionNumber) {
        super(InstructionData.DECREASE, InstructionType.BASIC, variable, label, origin,  instructionNumber);
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new DecreaseInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Instruction cloneInstruction(Variable targetVariable, Label label, Label referencesLabel, Instruction origin, int instructionNumber) {
        return new DecreaseInstruction(targetVariable, label, origin, instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getTargetVariable());
        long newVariableValue = Math.max(variableValue - 1, 0);

        context.updateVariable(getTargetVariable() ,newVariableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(variableRepresentation);
        command.append(" - 1");

        return command.toString();
    }

}
