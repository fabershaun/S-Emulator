package instruction;

import execution.ExecutionContext;
import label.Label;
import program.Program;
import variable.Variable;

import java.util.Map;


public class OriginOfAllInstruction extends AbstractInstruction {


    public OriginOfAllInstruction(Program mainProgram, Program programOfThisInstruction) {
        super(mainProgram, programOfThisInstruction, InstructionData.ORIGIN, InstructionType.BASIC, Variable.RESULT, null, 0);
    }

    @Override
    public String getCommand() {
        return "";
    }

    @Override
    public Label execute(ExecutionContext context) {
        return null;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return null;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin, Program mainProgram) {
        return null;
    }
}
