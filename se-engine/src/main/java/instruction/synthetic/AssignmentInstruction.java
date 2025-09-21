package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.DecreaseInstruction;
import instruction.basic.IncreaseInstruction;
import instruction.basic.JumpNotZeroInstruction;
import instruction.basic.NoOpInstruction;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssignmentInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final int MAX_DEGREE = 2;
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Variable sourceVariable;

    public AssignmentInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Variable sourceVariable, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.ASSIGNMENT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.sourceVariable = sourceVariable;
    }

    public AssignmentInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Label label, Variable sourceVariable, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.ASSIGNMENT, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.sourceVariable = sourceVariable;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new AssignmentInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), sourceVariable, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long sourceVariableValue = context.getVariableValue(sourceVariable);
        context.updateVariable(getTargetVariable(), sourceVariableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String sourceVariableRepresentation = sourceVariable.getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(targetVariableRepresentation);
        command.append(" <- ");
        command.append(sourceVariableRepresentation);

        return command.toString();
    }

    @Override
    public Variable getSourceVariable() {
        return sourceVariable;
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
        Variable workVariable1 = super.getMainProgram().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 =  super.getMainProgram().generateUniqueLabel();
        Label newLabel3 =  super.getMainProgram().generateUniqueLabel();
        Label newLabel4 =  super.getMainProgram().generateUniqueLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new ZeroVariableInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(), newLabel1,this, instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), sourceVariable, newLabel2, this, instructionNumber++));
        innerInstructions.add(new GotoLabelInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel4, this, instructionNumber++));
        innerInstructions.add(new DecreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), sourceVariable, newLabel2, this, instructionNumber++));
        innerInstructions.add(new IncreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, this, instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), sourceVariable, newLabel2, this, instructionNumber++));

        innerInstructions.add(new DecreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel3, this, instructionNumber++));
        innerInstructions.add(new IncreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(), this, instructionNumber++));
        innerInstructions.add(new IncreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), sourceVariable, this, instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel3, this, instructionNumber++));

        innerInstructions.add(new NoOpInstruction(getMainProgram(), getProgramOfThisInstruction(), super.getTargetVariable(), newLabel4, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Variable newSourceVariable = variableMap.getOrDefault(this.getSourceVariable(), this.getSourceVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new AssignmentInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, newSourceVariable, origin, newInstructionNumber);
    }
}
