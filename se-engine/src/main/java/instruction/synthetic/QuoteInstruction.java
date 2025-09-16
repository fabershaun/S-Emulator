package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import label.FixedLabel;
import label.Label;
import program.Program;
import program.ProgramImpl;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class QuoteInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final String functionName;
    private final String functionArguments;
    private int maxDegree;
    private Program  function;        // To know the labels and variables that the function used
    private final Program program;    // To know the labels and variables that already exist in program
    private final List<Instruction> innerInstructions = new ArrayList<>();

    public QuoteInstruction(Variable targetVariable, Label label, Instruction origin, int instructionNumber, String functionName, String functionArguments, Program program) {
        super(InstructionData.QUOTATION, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.functionName = functionName;
        this.functionArguments = functionArguments;
        this.program = program;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new QuoteInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber, functionName, functionArguments, program);
    }

    public void setFunctionForQuoteInstruction() {
        if (program instanceof ProgramImpl mainProgram) {
            this.function = mainProgram.getFunctions()
                    .stream()
                    .filter(arg -> arg.getName().equalsIgnoreCase(this.functionName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Function not found: " + functionName));
        }
    }

    @Override
    public Label execute(ExecutionContext context) {
        return null;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("(");
        command.append(targetVariableRepresentation);
        command.append(" <- ");
        command.append(functionArguments);
        command.append(")");
        return command.toString();
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int getMaxDegree() {
        return this.maxDegree;
    }

    @Override
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        int instructionNumber = startNumber;




        this.maxDegree = calculateMaxDegree();
        return instructionNumber;
    }

    private int calculateMaxDegree() {
        return innerInstructions.stream()
                .mapToInt(instruction -> {
                    if (instruction instanceof SyntheticInstruction synthetic) {
                        return synthetic.getMaxDegree();
                    }
                    return 0; // If basic instruction
                })
                .max()
                .orElse(0);
    }
}
