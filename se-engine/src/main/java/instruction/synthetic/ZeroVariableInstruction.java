package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.DecreaseInstruction;
import instruction.basic.JumpNotZeroInstruction;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZeroVariableInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final int MAX_DEGREE = 1;
    private final List<Instruction> innerInstructions = new ArrayList<>();;

    public ZeroVariableInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.ZERO_VARIABLE, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY, origin, instructionNumber);
    }

    public ZeroVariableInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Label label, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.ZERO_VARIABLE, InstructionType.SYNTHETIC, variable, label, origin, instructionNumber);
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new ZeroVariableInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getTargetVariable() ,0);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(0);

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
        int instructionNumber = startNumber;
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? super.getMainProgram().generateUniqueLabel() : super.getLabel();

        innerInstructions.add(new DecreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(), newLabel1, this,  instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(), newLabel1, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new ZeroVariableInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, this.getOriginalInstruction(), newInstructionNumber);
    }
}
