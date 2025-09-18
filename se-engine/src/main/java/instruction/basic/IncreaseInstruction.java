package instruction.basic;

import execution.ExecutionContext;
import instruction.AbstractInstruction;
import instruction.Instruction;
import instruction.InstructionData;
import instruction.InstructionType;
import label.Label;
import label.FixedLabel;
import variable.Variable;

import java.util.Map;

public class IncreaseInstruction extends AbstractInstruction {

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
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(variableRepresentation);
        command.append(" + 1");

        return command.toString();
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new IncreaseInstruction(newTargetVariable, newLabel, this.getOriginalInstruction(), newInstructionNumber);
    }
}
