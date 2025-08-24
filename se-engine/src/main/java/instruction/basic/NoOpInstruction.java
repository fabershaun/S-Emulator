package instruction.basic;

import execution.ExecutionContext;
import instruction.AbstractInstruction;
import instruction.Instruction;
import instruction.InstructionData;
import instruction.InstructionType;
import label.FixedLabel;
import label.Label;
import variable.Variable;

public class NoOpInstruction extends AbstractInstruction {

    public NoOpInstruction(Variable variable, Instruction origin, int instructionNumber) {
        super(InstructionData.NO_OP, InstructionType.BASIC ,variable, FixedLabel.EMPTY, origin ,instructionNumber);
    }

    public NoOpInstruction(Variable variable, Label label, Instruction origin, int instructionNumber) {
        super(InstructionData.NO_OP, InstructionType.BASIC, variable, label,  origin, instructionNumber);
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new NoOpInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(variableRepresentation);

        return command.toString();
    }
}
