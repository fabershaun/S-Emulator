package instruction.basic;

import execution.ExecutionContext;
import instruction.AbstractInstruction;
import instruction.Instruction;
import instruction.InstructionData;
import instruction.InstructionType;
import label.Label;
import label.FixedLabel;
import program.Program;
import variable.Variable;

import java.util.Map;

public class DecreaseInstruction extends AbstractInstruction {

    public DecreaseInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.DECREASE, InstructionType.BASIC, variable, FixedLabel.EMPTY, origin, instructionNumber);
    }

    public DecreaseInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Label label, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.DECREASE, InstructionType.BASIC, variable, label, origin,  instructionNumber);
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new DecreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber);
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

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new DecreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, this, newInstructionNumber);
    }
}
