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

import java.util.*;

public class QuoteInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final String functionName;
    private final String functionArgumentsStrNotTrimmed;
    private final List<QuoteArgument> quoteArguments =  new ArrayList<>();
    private int currentCyclesNumber;
    private int maxDegree = 4; // Dynamic

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Map<Variable, Variable> mapFunctionToProgramVariable = new HashMap<>();
    private final Map<Program, Variable> mapQuoteFunctionToFunctionVariable = new HashMap<>();
    private final Map<Label, Label> mapFunctionToProgramLabel = new HashMap<>();

    public QuoteInstruction(Program programOfThisInstruction, Variable targetVariable, Label label, Instruction origin, int instructionNumber, String functionName, String functionArgumentsStrNotTrimmed) {
        super(programOfThisInstruction, InstructionData.QUOTATION, InstructionType.SYNTHETIC ,targetVariable, label, origin, instructionNumber);
        this.functionName = functionName;
        this.functionArgumentsStrNotTrimmed = functionArgumentsStrNotTrimmed;
    }

    public void initializeQuoteInstruction() {
        extractQuoteArguments();
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new QuoteInstruction(getProgramOfThisInstruction(), getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber, this.functionName, this.functionArgumentsStrNotTrimmed);
    }

    @Override
    public Label execute(ExecutionContext context) {
        ProgramExecutor functionExecutor = new ProgramExecutorImpl(this.getFunctionOfThisInstruction());

        List<Long> inputs = new ArrayList<>();
        for (QuoteArgument quoteArgument : this.quoteArguments) {
            switch (quoteArgument.getType()) {
                case VARIABLE -> inputs.add(quoteArgument.getValue());
                case FUNCTION -> {
                    long functionResult = calculateFunctionResult(quoteArgument);
                    inputs.add(functionResult);
                }
            }
        }

        // Run
        functionExecutor.run(0, inputs.toArray(Long[]::new));

        // Update value in parent program
        Variable resultVariable = this.getFunctionOfThisInstruction().getResultVariable();
        long quoteFunctionResult = functionExecutor.getVariableValue(resultVariable);
        context.updateVariable(getTargetVariable(), quoteFunctionResult);

        currentCyclesNumber = InstructionData.QUOTATION.getCycles() + functionExecutor.getTotalCyclesOfProgram();
        return FixedLabel.EMPTY;
    }

    // Recursive function: the goal is to reach to a functions that hold only variable arguments
    private long calculateFunctionResult(QuoteArgument quoteArgument) {
        FunctionArgument innerFunction = (FunctionArgument) quoteArgument;
        List<Long> inputs = new ArrayList<>();

        for(QuoteArgument quoteFunctionArgument : innerFunction.getArguments()) {
            switch (quoteFunctionArgument.getType()) {
                case FUNCTION -> {
                    long functionResult = calculateFunctionResult(quoteFunctionArgument);
                    inputs.add(functionResult);
                }
                case VARIABLE -> inputs.add(quoteFunctionArgument.getValue());
            }
        }

        ProgramExecutor functionExecutor = new ProgramExecutorImpl(innerFunction.getFunction());

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
        command.append(targetVariableRepresentation);
        command.append(" <- ");
        command.append("(");
        command.append(userString);
        command.append(arguments);
        command.append(")");
        return command.toString();
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int getMaxDegree() {
        return this.maxDegree;  // TODO: אולי לחשב כאן את כל הדרגות בצודה דינמאית
    }

    @Override
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        List<Instruction> expandedInstructions = convertFunctionData(startNumber);
        innerInstructions.clear();
        innerInstructions.addAll(expandedInstructions);

        return startNumber + innerInstructions.size();
    }

    @Override
    public int getCycleOfInstruction() {
        return (this.currentCyclesNumber == 0) ? InstructionData.QUOTATION.getCycles() : this.currentCyclesNumber;
    }

    public String getQuoteFunctionName() {
        return this.functionName;
    }

    public Program getFunctionOfThisInstruction() {
        return super.getProgramOfThisInstruction().getFunctionsHolder().getFunctionByName(this.functionName);
    }

    private List<Instruction> convertFunctionData(int startNumber) {
        List<Instruction> expandedInstructions = new ArrayList<>();

//        mapQuoteData();
        mapQuoteFunctionVariables();
        mapQuoteFunctionLabels();
        mapQuoteFunctionVariableToProgramFunction();

        int instructionNumber = startNumber;

        instructionNumber = addParameterInstructions(expandedInstructions, instructionNumber);       // Step 1: assign arguments to function input variables
        instructionNumber = addClonedFunctionInstructions(expandedInstructions, instructionNumber); // Step 2: clone function instructions with variable/label remapping
        addResultAssignment(expandedInstructions, instructionNumber);

        return expandedInstructions;
    }

//    private void mapQuoteData() {
//        for (QuoteArgument quoteArgument : this.quoteArguments) {
//            switch (quoteArgument.getType()) {
//                case VARIABLE -> {
//                    Variable originalVariable = (Variable) quoteArgument;
//                    Variable newWorkVariable = getProgramOfThisInstruction().generateUniqueVariable();
//                    mapFunctionToProgramVariable.put(originalVariable, newWorkVariable);
//                }
//                case FUNCTION -> {
//                    FunctionArgument functionArgument = (FunctionArgument) quoteArgument;
//                    Program function = functionArgument.getFunction();
//                    Variable newWorkVariable = getProgramOfThisInstruction().generateUniqueVariable();
//                    mapQuoteFunctionToFunctionVariable.put(function, newWorkVariable);
//                    mapFunctionLabels(function);
//                }
//            }
//        }
//    }
//
//    private void mapFunctionLabels(Program function) {
//        // Map each function label to a unique label in the caller program
//        for (Label functionLabel : getFunctionOfThisInstruction().getLabelsInProgram()) {
//            Label newLabel = super.getProgramOfThisInstruction().generateUniqueLabel();
//            mapFunctionToProgramLabel.put(functionLabel, newLabel);
//        }
//    }

    private void mapQuoteFunctionVariables() {
        Program mainProgram = super.getProgramOfThisInstruction();

        for (Variable functionInputVariable : getFunctionOfThisInstruction().getInputVariables()) {
            Variable newWorkVariable = mainProgram.generateUniqueVariable();
            mapFunctionToProgramVariable.put(functionInputVariable, newWorkVariable);
        }

        Variable functionResult = getFunctionOfThisInstruction().getResultVariable();
        Variable newWorkVariable = mainProgram.generateUniqueVariable();
        mapFunctionToProgramVariable.put(functionResult , newWorkVariable);

    }

    private void mapQuoteFunctionLabels() {
        // Map each function label to a unique label in the caller program
        for (Label functionLabel : getFunctionOfThisInstruction().getLabelsInProgram()) {
            Label newLabel = super.getProgramOfThisInstruction().generateUniqueLabel();
            mapFunctionToProgramLabel.put(functionLabel, newLabel);
        }
    }

    private void mapQuoteFunctionVariableToProgramFunction() {

        for(QuoteArgument quoteArgument : quoteArguments) {
            if (quoteArgument instanceof FunctionArgument functionArgument) {
                Variable newWorkVariable = super.getProgramOfThisInstruction().generateUniqueVariable();
                mapQuoteFunctionToFunctionVariable.put(functionArgument.getFunction(), newWorkVariable);
            }
        }
    }

    private int addParameterInstructions(List<Instruction> targetList, int instructionNumber) {
        Label originalLabel = getLabel();
        boolean firstAssignment = true;

        for(QuoteArgument quoteArgument : quoteArguments) {
            Label labelForThisInstruction = (firstAssignment && (originalLabel != null) && (originalLabel != FixedLabel.EMPTY))
                    ? originalLabel
                    : FixedLabel.EMPTY;
            firstAssignment = false;

            switch (quoteArgument.getType()) {
                case VARIABLE -> {
                    VariableArgument variableArgument = (VariableArgument) quoteArgument;
                    Variable sourceVariable = variableArgument.getVariable();                      // sourceVariable = original Quote instruction input variable
                    Variable targetVariable = mapFunctionToProgramVariable.get(sourceVariable);    // targetVariable = new work variable that we created

                    // create assignment: targetVariable <- sourceVariable
                    targetList.add(
                            new AssignmentInstruction(getProgramOfThisInstruction(), targetVariable, labelForThisInstruction, sourceVariable, getOriginalInstruction(), instructionNumber++));
                }

                case FUNCTION -> {
                    FunctionArgument functionArgument = (FunctionArgument) quoteArgument;
                    Program functionInArguments = functionArgument.getFunction();
                    Variable targetVariable = mapQuoteFunctionToFunctionVariable.get(functionInArguments);    // targetVariable = new work variable that we created
                    String innerFunctionName = functionArgument.getFunction().getName();
                    String innerFunctionArgumentsStr = functionArgument.getArgumentStr();

                    // create quote instruction: targetVariable <- (functionName, functionArguments...)
                    targetList.add(
                            new QuoteInstruction(getProgramOfThisInstruction(), targetVariable, labelForThisInstruction, getOriginalInstruction(), instructionNumber++, innerFunctionName, innerFunctionArgumentsStr));
                    }
            }
        }

        return instructionNumber;
    }

    // Clone function instructions with remapped variables and labels
    private int addClonedFunctionInstructions(List<Instruction> targetList, int instructionNumber) {
        for (Instruction functionInstruction : getFunctionOfThisInstruction().getInstructionsList()) {
            Instruction cloned = functionInstruction.remapAndClone(
                    instructionNumber++,
                    mapFunctionToProgramVariable,
                    mapFunctionToProgramLabel
            );

            targetList.add(cloned);
        }
        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable()); // TODO: check
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new QuoteInstruction(getProgramOfThisInstruction(), newTargetVariable, newLabel, this.getOriginalInstruction(), newInstructionNumber, this.functionName, this.functionArgumentsStrNotTrimmed);
    }

    // Assign function result back to the target of this Quote
    private void addResultAssignment(List<Instruction> targetList, int instructionNumber) {
        Variable mappedResult = mapFunctionToProgramVariable.get(Variable.RESULT);

        if (mappedResult == null) {
            throw new IllegalStateException("Function result variable not mapped correctly");
        }

        Label lastLabel = mapFunctionToProgramLabel.getOrDefault(FixedLabel.EXIT,  FixedLabel.EMPTY);

        targetList.add(
                new AssignmentInstruction(getProgramOfThisInstruction(), getTargetVariable(), lastLabel, mappedResult, getOriginalInstruction(), instructionNumber));
    }

    private void extractQuoteArguments() {
        List<String> argumentsStr = extractQuoteArgumentsToStrList();
        for(String argumentStr : argumentsStr) {
            if(argumentStr.startsWith("(") && argumentStr.endsWith(")")) {
                quoteArguments.add(new FunctionArgument(getFunctionOfThisInstruction(), argumentStr));  // If function
            } else {
                quoteArguments.add(new VariableArgument(getFunctionOfThisInstruction(), argumentStr));  // If variable
            }
        }
    }

    private List<String> extractQuoteArgumentsToStrList() {
        if (functionArgumentsStrNotTrimmed == null || functionArgumentsStrNotTrimmed.trim().isEmpty()) {
            return List.of();
        }

        String argumentsStr = functionArgumentsStrNotTrimmed.trim();
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
        if (functionArgumentsStrNotTrimmed == null || functionArgumentsStrNotTrimmed.isBlank()) {
            return "";
        }

        String argsStr = functionArgumentsStrNotTrimmed.trim();
        StringBuilder result = new StringBuilder();
        StringBuilder current = new StringBuilder();

        for (char c : argsStr.toCharArray()) {
            if (c == ',' || c == '(' || c == ')') {
                if (!current.isEmpty()) {
                    String token = current.toString().trim();
                    if (checkIfFunction(token)) {
                        String userString = super.getProgramOfThisInstruction()
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
                String userString = super.getProgramOfThisInstruction()
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
        Program function = super.getProgramOfThisInstruction().getFunctionsHolder().getFunctionByName(current);
        return function != null;
    }
}
