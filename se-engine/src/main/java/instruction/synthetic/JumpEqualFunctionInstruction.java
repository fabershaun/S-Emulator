package instruction.synthetic;

import execution.ExecutionContext;
import execution.ProgramExecutor;
import execution.ProgramExecutorImpl;
import instruction.*;
import instruction.synthetic.quoteArguments.FunctionArgument;
import instruction.synthetic.quoteArguments.QuoteArgument;
import instruction.synthetic.quoteArguments.VariableArgument;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;
import variable.VariableType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;

public class JumpEqualFunctionInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final String referenceFunctionName;
    private final String referenceFunctionArguments;
    private final Label referenceLabel;
    private int currentCyclesNumber;
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final List<QuoteArgument> quoteArguments =  new ArrayList<>();

    public JumpEqualFunctionInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Label label, Label referenceLabel, String referenceFunctionName, String referenceFunctionArguments, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_EQUAL_FUNCTION, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.referenceFunctionName = referenceFunctionName;
        this.referenceFunctionArguments = referenceFunctionArguments;
        this.referenceLabel = referenceLabel;
    }

    public void initializeInstruction() {
        extractQuoteArguments();
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpEqualFunctionInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), referenceLabel, referenceFunctionName, referenceFunctionArguments, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long targetVariableValue = context.getVariableValue(getTargetVariable());

        ProgramExecutor functionExecutor = new ProgramExecutorImpl(this.getFunctionOfThisInstruction());
        initializeInstruction();
        List<Long> inputs = getInputs(quoteArguments, context);

        // Run
        functionExecutor.run(0, inputs.toArray(Long[]::new));

        // Update value in parent program
        Variable resultVariable = this.getFunctionOfThisInstruction().getResultVariable();
        long functionResult = functionExecutor.getVariableValue(resultVariable);

        // Update cycles number
        currentCyclesNumber = InstructionData.JUMP_EQUAL_FUNCTION.getCycles() + functionExecutor.getTotalCyclesOfProgram();

        return (targetVariableValue == functionResult) ? referenceLabel : FixedLabel.EMPTY;
    }

    private List<Long> getInputs(List<QuoteArgument> innerQuoteArgumentsList, ExecutionContext context) {
        List<Long> inputs = new ArrayList<>();

        for (QuoteArgument quoteFunctionArgument : innerQuoteArgumentsList) {
            switch (quoteFunctionArgument.getType()) {
                case FUNCTION -> {
                    FunctionArgument functionArgument = (FunctionArgument) quoteFunctionArgument;
                    long functionResult = calculateFunctionResult(functionArgument, context);
                    inputs.add(functionResult);
                }
                case VARIABLE -> {
                    VariableArgument innerVariableArgument = (VariableArgument) quoteFunctionArgument;
                    long inputValue = 0;
                    if (innerVariableArgument.getVariable().getType().equals(VariableType.INPUT)) {
                        inputValue = innerVariableArgument.getInputValueFromContext(context);       // If it's input variable
                    } else if (innerVariableArgument.getVariable().getType().equals(VariableType.WORK)) {
                        inputValue = innerVariableArgument.getValue();  // If it's work variable
                    }
                    inputs.add(inputValue);
                }
            }
        }

        return inputs;
    }

    // Recursive function: the goal is to reach to a functions that hold only variable arguments
    private long calculateFunctionResult(FunctionArgument innerFunction, ExecutionContext context) {

        ProgramExecutor functionExecutor = new ProgramExecutorImpl(innerFunction.getFunction());
        List<Long> inputs = getInputs(innerFunction.getArguments(), context);

        // Run
        functionExecutor.run(0, inputs.toArray(Long[]::new));

        // Return function result
        Variable resultVariable = innerFunction.getFunction().getResultVariable();
        return functionExecutor.getVariableValue(resultVariable);
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String userString = getFunctionOfThisInstruction().getUserString();
        String arguments = replaceFunctionsWithUserStringsStrict();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(targetVariableRepresentation);
        command.append(" = ");
        command.append("(");
        command.append(userString);
        command.append(arguments);
        command.append(")");
        command.append(" GOTO ");
        command.append(referenceLabel.getLabelRepresentation());

        return command.toString();
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int getCycleOfInstruction() {
        return (this.currentCyclesNumber == 0) ? InstructionData.JUMP_EQUAL_FUNCTION.getCycles() : this.currentCyclesNumber;
    }

    @Override
    public int getMaxDegree() {

        int maxDegree = 0;
        this.setInnerInstructionsAndReturnTheNextOne(0);
        for(Instruction instruction : this.innerInstructions) {
            if(instruction instanceof SyntheticInstruction syntheticInstruction) {
                maxDegree = max(maxDegree, syntheticInstruction.getMaxDegree());
            }

        }

        return maxDegree + 1;
    }

//    @Override
//    public int getMaxDegree() {
//        return helperGetMaxDegree(getFunctionOfThisInstruction(), quoteArguments);
//    }
//
//    // Recursive function
//    private int helperGetMaxDegree(Program innerFunction, List<QuoteArgument> innerQuoteArguments) {
//        int maxDegreeOfQuoteFunction = innerFunction.calculateProgramMaxDegree();    // Calculate inner quote's function degree
//        int maxDegreeOfQuoteFunctionArguments = calculateDegreeOfQuoteFunctionArguments(innerQuoteArguments); // Calculate inner quote's function's arguments degree
//
//        return maxDegreeOfQuoteFunction + maxDegreeOfQuoteFunctionArguments;
//    }
//
//    // Recursive function
//    private int calculateDegreeOfQuoteFunctionArguments(List<QuoteArgument> innerQuoteArguments) {
//        int degree = 0;
//        int maxDegreeOfQuoteFunctionArguments = 0;
//
//        for(QuoteArgument quoteArgument : innerQuoteArguments) {
//            if(quoteArgument instanceof FunctionArgument innerFunctionArgument) {
//                Program innerFunction = innerFunctionArgument.getFunction();
//                degree = helperGetMaxDegree(innerFunction, innerFunctionArgument.getArguments());
//                maxDegreeOfQuoteFunctionArguments = max(maxDegreeOfQuoteFunctionArguments, degree);
//            }
//        }
//
//        return maxDegreeOfQuoteFunctionArguments;
//    }

    @Override
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        Variable workVariable1 = super.getMainProgram().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        int instructionNumber = startNumber;

        innerInstructions.clear();
        innerInstructions.add(new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel1, getOriginalInstruction(), instructionNumber++, referenceFunctionName, referenceFunctionArguments));
        innerInstructions.add(new JumpEqualVariableInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), workVariable1, referenceLabel, getOriginalInstruction(), instructionNumber++));
        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction newOrigin, Program newMainProgram) {
        setMainProgram(newMainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new JumpEqualFunctionInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, referenceLabel, referenceFunctionName, referenceFunctionArguments, newOrigin, newInstructionNumber);
    }

    public Program getFunctionOfThisInstruction() {
        return super.getMainProgram().getFunctionsHolder().getFunctionByName(this.referenceFunctionName);
    }

    private void extractQuoteArguments() {
        quoteArguments.clear();
        List<String> argumentsStr = extractQuoteArgumentsToStrList();
        for(String argumentStr : argumentsStr) {
            if(argumentStr.startsWith("(") && argumentStr.endsWith(")")) {
                quoteArguments.add(new FunctionArgument(getMainProgram(), getFunctionOfThisInstruction(), argumentStr));  // If function
            } else {
                quoteArguments.add(new VariableArgument(getMainProgram(), getFunctionOfThisInstruction(), argumentStr));  // If variable
            }
        }
    }

    private List<String> extractQuoteArgumentsToStrList() {
        if (referenceFunctionArguments == null || referenceFunctionArguments.trim().isEmpty()) {
            return List.of();
        }

        String argumentsStr = referenceFunctionArguments.trim();
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenthesesDepth = 0;

        for (char c : argumentsStr.toCharArray()) {
            if (c == ',' && parenthesesDepth == 0) {
                tokens.add(current.toString().trim());
                current.setLength(0);
            } else {
                if (c == '(') parenthesesDepth++;
                if (c == ')') parenthesesDepth--;
                current.append(c);
            }
        }

        if (!current.isEmpty()) {                   // To add the last part or to add the argument if there were no parentheses in the original string
            tokens.add(current.toString().trim());
        }

        return tokens;
    }

    private String replaceFunctionsWithUserStringsStrict() {
        if (referenceFunctionArguments == null || referenceFunctionArguments.isBlank()) {
            return "";
        }

        String argsStr = referenceFunctionArguments.trim();
        StringBuilder result = new StringBuilder();
        StringBuilder current = new StringBuilder();

        for (char c : argsStr.toCharArray()) {
            if (c == ',' || c == '(' || c == ')') {
                if (!current.isEmpty()) {
                    String token = current.toString().trim();
                    if (checkIfFunction(token)) {
                        String userString = super.getMainProgram()
                                .getFunctionsHolder()
                                .getFunctionByName(token)
                                .getUserString();

                        result.append(userString);
                    } else {
                        result.append(token);
                    }
                    current.setLength(0);
                }
                // always keep the delimiter
                result.append(c);
            } else {
                current.append(c);
            }
        }

        // handle last token (if exists)
        if (!current.isEmpty()) {
            String token = current.toString().trim();
            if (checkIfFunction(token)) {
                String userString = super.getMainProgram()
                        .getFunctionsHolder()
                        .getFunctionByName(token)
                        .getUserString();
                result.append(userString);
            } else {
                result.append(token);
            }
        }

        result.insert(0,",");
        return result.toString();
    }

    private boolean checkIfFunction(String current) {
        Program function = super.getMainProgram().getFunctionsHolder().getFunctionByName(current);
        return function != null;
    }
}

