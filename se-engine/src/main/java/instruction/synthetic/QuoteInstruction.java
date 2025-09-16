package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import label.FixedLabel;
import label.Label;
import program.Program;
import program.ProgramImpl;
import variable.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final String functionName;
    private final String functionArguments;
    private int maxDegree;
    private Program  function;        // To know the labels and variables that the function used
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private Map<Variable, Variable> functionToProgramVariable = new HashMap<>();
    private Map<Label, Label> functionToProgramLabel = new HashMap<>();

    public QuoteInstruction(Variable targetVariable, Label label, Instruction origin, int instructionNumber, String functionName, String functionArguments) {
        super(InstructionData.QUOTATION, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.functionName = functionName;
        this.functionArguments = functionArguments;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new QuoteInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber, functionName, functionArguments);
    }

    public void setFunctionForQuoteInstruction() {
        if (super.getProgramOfThisInstruction() instanceof ProgramImpl mainProgram) {
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
        List<Instruction> functionInstructionConverted = convertFunctionData(startNumber);
        innerInstructions.addAll(functionInstructionConverted);


        this.maxDegree = calculateMaxDegree();
        return startNumber + innerInstructions.size();
    }

    private List<Instruction> convertFunctionData(int startNumber) {
        List<Instruction> functionInstructionConverted = new ArrayList<>();

        mapFunctionVariables();
        mapFunctionLabels();

        // פקודות השמה N

        for (Instruction functionInstruction : function.getInstructionsList()) {

        }

        return functionInstructionConverted;
    }

    private void mapFunctionLabels() {
        Program mainProgram = super.getProgramOfThisInstruction();

        for (Label functionLabel : mainProgram.getLabelsInProgram()) {
            Label newLabel = mainProgram.generateUniqueLabel();
            functionToProgramLabel.put(functionLabel, newLabel);
        }

        for (Variable functionWorkVariable : mainProgram.getWorkVariables()) {
            Variable newWorkVariable = mainProgram.generateUniqueVariable();
            functionToProgramLabel.put(functionWorkVariable, newWorkVariable);
        }
    }

    private void mapFunctionVariables() {
        Program mainProgram = super.getProgramOfThisInstruction();

        for (Variable functionInputVariable : mainProgram.getInputVariables()) {
            Variable newWorkVariable = mainProgram.generateUniqueVariable();
            functionToProgramVariable.put(functionInputVariable, newWorkVariable);
        }

        for (Variable functionWorkVariable : mainProgram.getWorkVariables()) {
            Variable newWorkVariable = mainProgram.generateUniqueVariable();
            functionToProgramVariable.put(functionWorkVariable, newWorkVariable);
        }

        //Variable newWorkVariable = mainProgram.generateUniqueVariable();
        //functionVariableToProgramVariable.put( , newWorkVariable);     // TODO: map 'y' of function to program

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
