package engine.logic.programData.instruction.synthetic;

import engine.logic.execution.ExecutionContext;
import engine.logic.programData.instruction.*;
import engine.logic.programData.instruction.*;
import engine.logic.programData.instruction.basic.IncreaseInstruction;
import engine.logic.programData.instruction.basic.JumpNotZeroInstruction;
import engine.logic.programData.label.FixedLabel;
import engine.logic.programData.label.Label;
import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GotoLabelInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencesLabel;

    public GotoLabelInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY, origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    public GotoLabelInstruction(Program mainProgram, Program programOfThisInstruction, Variable variable, Label label, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC, variable, label, origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new GotoLabelInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), referencesLabel, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        return referencesLabel;
    }

    @Override
    public String getCommand() {
        String labelRepresentation = referencesLabel.getLabelRepresentation();

        String command = "GOTO " +
                labelRepresentation;

        return command;
    }

    @Override
    public Label getReferenceLabel() {
        return referencesLabel;
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int expandInstruction(int startNumber) {
        Variable workVariable1 = super.getMainProgram().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new IncreaseInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel1, this, instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, this.referencesLabel, this,  instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin, Program mainProgram) {
        setMainProgram(mainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());
        Label newReferenceLabel = labelMap.getOrDefault(this.getReferenceLabel(), this.getReferenceLabel());

        return new GotoLabelInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, newReferenceLabel, origin, newInstructionNumber);
    }
}
