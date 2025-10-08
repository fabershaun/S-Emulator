package engine.logic.instruction.synthetic;

import engine.logic.execution.ExecutionContext;
import engine.logic.instruction.*;
import instruction.*;
import engine.logic.instruction.basic.DecreaseInstruction;
import engine.logic.instruction.basic.JumpNotZeroInstruction;
import engine.logic.label.FixedLabel;
import engine.logic.label.Label;
import engine.logic.program.Program;
import engine.logic.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZeroVariableInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final List<Instruction> innerInstructions = new ArrayList<>();

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

        String command = variableRepresentation +
                " <- " +
                0;

        return command;
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int expandInstruction(int startNumber) {
        int instructionNumber = startNumber;
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? super.getMainProgram().generateUniqueLabel() : super.getLabel();

        innerInstructions.add(new DecreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(), newLabel1, this,  instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(), newLabel1, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin, Program mainProgram) {
        setMainProgram(mainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new ZeroVariableInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, origin, newInstructionNumber);
    }
}
