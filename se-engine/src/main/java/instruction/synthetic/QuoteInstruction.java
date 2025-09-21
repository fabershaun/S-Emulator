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

import java.util.*;

import static java.lang.Math.max;


public class QuoteInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final String functionName;
    private final String functionArgumentsStrNotTrimmed;
    private final List<QuoteArgument> quoteArguments =  new ArrayList<>();
    private int currentCyclesNumber;
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Map<Variable, Variable> mapFunctionToProgramVariable = new LinkedHashMap<>();
    private final Map<Label, Label> mapFunctionToProgramLabel = new HashMap<>();

    public QuoteInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Label label, Instruction origin, int instructionNumber, String functionName, String functionArgumentsStrNotTrimmed) {
        super(mainProgram, programOfThisInstruction, InstructionData.QUOTATION, InstructionType.SYNTHETIC ,targetVariable, label, origin, instructionNumber);
        this.functionName = functionName;
        this.functionArgumentsStrNotTrimmed = functionArgumentsStrNotTrimmed;
    }

    public void initializeQuoteInstruction() {
        extractQuoteArguments();
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber, this.functionName, this.functionArgumentsStrNotTrimmed);
    }

    @Override
    public Label execute(ExecutionContext context) {
        ProgramExecutor functionExecutor = new ProgramExecutorImpl(this.getFunctionOfThisInstruction());
        initializeQuoteInstruction();
        List<Long> inputs = getInputs(quoteArguments, context);

        // Run
        functionExecutor.run(0, inputs.toArray(Long[]::new));

        // Update value in parent program
        Variable resultVariable = this.getFunctionOfThisInstruction().getResultVariable();
        long quoteFunctionResult = functionExecutor.getVariableValue(resultVariable);
        context.updateVariable(getTargetVariable(), quoteFunctionResult);

        currentCyclesNumber = InstructionData.QUOTATION.getCycles() + functionExecutor.getTotalCyclesOfProgram();
        return FixedLabel.EMPTY;
    }

    private List<Long> getInputs(List<QuoteArgument> innerQuoteArgumentsList, ExecutionContext context) {
        List<Long> inputs = new ArrayList<>();

        for(QuoteArgument quoteFunctionArgument : innerQuoteArgumentsList) {
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


//    @Override
//    public Label execute(ExecutionContext context) {
//        ProgramExecutor functionExecutor = new ProgramExecutorImpl(this.getFunctionOfThisInstruction());
//
//        List<Long> inputs = new ArrayList<>();
//        inputs = getInputs(quoteArguments, context);
//
//        for (QuoteArgument quoteArgument : this.quoteArguments) {
//            switch (quoteArgument.getType()) {
//                case VARIABLE -> inputs.add(quoteArgument.getValue());
//                case FUNCTION -> {
//                    long functionResult = calculateFunctionResult(quoteArgument, context);
//                    inputs.add(functionResult);
//                }
//            }
//        }
//
//        // Run
//        functionExecutor.run(0, inputs.toArray(Long[]::new));
//
//        // Update value in parent program
//        Variable resultVariable = this.getFunctionOfThisInstruction().getResultVariable();
//        long quoteFunctionResult = functionExecutor.getVariableValue(resultVariable);
//        context.updateVariable(getTargetVariable(), quoteFunctionResult);
//
//        currentCyclesNumber = InstructionData.QUOTATION.getCycles() + functionExecutor.getTotalCyclesOfProgram();
//        return FixedLabel.EMPTY;
//    }
//
//    private List<Long> getInputs(List<QuoteArgument> innerQuoteArgumentsList, ExecutionContext context) {
//        List<Long> inputs = new ArrayList<>();
//
//        for(QuoteArgument quoteFunctionArgument : innerQuoteArgumentsList) {
//            switch (quoteFunctionArgument.getType()) {
//                case FUNCTION -> {
//                    long functionResult = calculateFunctionResult(quoteFunctionArgument, context);
//                    inputs.add(functionResult);
//                }
//                case VARIABLE -> {
//                    VariableArgument innerVariableArgument = (VariableArgument) quoteFunctionArgument;
//                    long inputValue = 0;
//                    if (innerVariableArgument.getVariable().getType().equals(VariableType.INPUT)) {
//                        inputValue = innerVariableArgument.getInputValueFromContext(context);       // If it's input variable
//                    } else if (innerVariableArgument.getVariable().getType().equals(VariableType.WORK)) {
//                        inputValue = innerVariableArgument.getValue();  // If it's work variable
//                    }
//                    inputs.add(inputValue);
//                }
//            }
//        }
//
//        return inputs;
//    }
//
//    // Recursive function: the goal is to reach to a functions that hold only variable arguments
//    private long calculateFunctionResult(List<Long> inputs, FunctionArgument innerFunction) {
//
//        FunctionArgument innerFunction = (FunctionArgument) quoteArgument;
//        for(QuoteArgument quoteFunctionArgument : innerFunction.getArguments()) {
//            switch (quoteFunctionArgument.getType()) {
//                case FUNCTION -> {
//                    long functionResult = calculateFunctionResult(quoteFunctionArgument, context);
//                    inputs.add(functionResult);
//                }
//                case VARIABLE -> {
//                    VariableArgument innerVariableArgument = (VariableArgument) quoteFunctionArgument;
//                    long inputValue = 0;
//                    if (innerVariableArgument.getVariable().getType().equals(VariableType.INPUT)) {
//                        inputValue = innerVariableArgument.getInputValueFromContext(context);       // If it's input variable
//                    } else if (innerVariableArgument.getVariable().getType().equals(VariableType.WORK)) {
//                        inputValue = innerVariableArgument.getValue();  // If it's work variable
//                    }
//                    inputs.add(inputValue);
//                }
//            }
//        }
//
//        ProgramExecutor functionExecutor = new ProgramExecutorImpl(innerFunction.getFunction());
//
//        // Run
//        functionExecutor.run(0, inputs.toArray(Long[]::new));
//
//        // Return function result
//        Variable resultVariable = innerFunction.getFunction().getResultVariable();
//        return functionExecutor.getVariableValue(resultVariable);
//    }

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

    // todo: check if right
    @Override
    public int getMaxDegree() {
        return helperGetMaxDegree(getFunctionOfThisInstruction(), quoteArguments);
    }

    // Recursive function
    private int helperGetMaxDegree(Program innerFunction, List<QuoteArgument> innerQuoteArguments) {
        int maxDegreeOfQuoteFunction = innerFunction.calculateProgramMaxDegree();    // Calculate inner quote's function degree
        int maxDegreeOfQuoteFunctionArguments = calculateDegreeOfQuoteFunctionArguments(innerQuoteArguments); // Calculate inner quote's function's arguments degree

        return maxDegreeOfQuoteFunction + maxDegreeOfQuoteFunctionArguments;
    }

    // Recursive function
    private int calculateDegreeOfQuoteFunctionArguments(List<QuoteArgument> innerQuoteArguments) {
        int degree = 0;
        int maxDegreeOfQuoteFunctionArguments = 0;

        for(QuoteArgument quoteArgument : innerQuoteArguments) {
            if(quoteArgument instanceof FunctionArgument innerFunctionArgument) {
                Program innerFunction = innerFunctionArgument.getFunction();
                degree = helperGetMaxDegree(innerFunction, innerFunctionArgument.getArguments());
                maxDegreeOfQuoteFunctionArguments = max(maxDegreeOfQuoteFunctionArguments, degree);
            }
        }

        return maxDegreeOfQuoteFunctionArguments;
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
        return super.getMainProgram().getFunctionsHolder().getFunctionByName(this.functionName);
    }

    private List<Instruction> convertFunctionData(int startNumber) {
        List<Instruction> expandedInstructions = new ArrayList<>();

        mapQuoteFunctionVariables();
        mapQuoteFunctionLabels();

        initializeQuoteInstruction();

        int instructionNumber = startNumber;
        instructionNumber = addParameterInstructions(expandedInstructions, instructionNumber);       // Step 1: assign arguments to function input variables
        instructionNumber = addClonedFunctionInstructions(expandedInstructions, instructionNumber);  // Step 2: clone function instructions with variable/label remapping
        addResultAssignment(expandedInstructions, instructionNumber);

        return expandedInstructions;
    }

    private void mapQuoteFunctionVariables() {

        Variable functionResult = getFunctionOfThisInstruction().getResultVariable();
        Variable newWorkVariable = getMainProgram().generateUniqueVariable();
        mapFunctionToProgramVariable.put(functionResult , newWorkVariable);

        for (Variable functionInputVariable : getFunctionOfThisInstruction().getInputVariables()) {
            newWorkVariable = getMainProgram().generateUniqueVariable();
            mapFunctionToProgramVariable.put(functionInputVariable, newWorkVariable);
        }

        for (Variable functionWorkVariable : getFunctionOfThisInstruction().getWorkVariables()) {
            newWorkVariable = getMainProgram().generateUniqueVariable();
            mapFunctionToProgramVariable.put(functionWorkVariable, newWorkVariable);
        }
    }

    private void mapQuoteFunctionLabels() {
        Set<Label> labelsInFunction = new HashSet<>();
        labelsInFunction.addAll(getFunctionOfThisInstruction().getLabelsInProgram());
        labelsInFunction.addAll(getFunctionOfThisInstruction().getReferenceLabelsInProgram());

        // Map each function label to a unique label in the caller program
        for (Label functionLabel : labelsInFunction) {              // To include Exit if exist
            Label newLabel = getMainProgram().generateUniqueLabel();
            mapFunctionToProgramLabel.put(functionLabel, newLabel);
        }
    }

    private int addParameterInstructions(List<Instruction> targetList, int instructionNumber) {
        Label originalLabel = getLabel();
        boolean firstAssignment = true;

        int indexOfQuoteArgumentList = 0;
        for (Variable inputVariableInFunction : getFunctionOfThisInstruction().getInputVariables()) {

            Label labelForThisInstruction = (firstAssignment && (originalLabel != null) && (originalLabel != FixedLabel.EMPTY))     // To put the first label
                    ? originalLabel
                    : FixedLabel.EMPTY;
            firstAssignment = false;

            QuoteArgument quoteArgument = quoteArguments.get(indexOfQuoteArgumentList);
            Variable workVariable = mapFunctionToProgramVariable.get(inputVariableInFunction);

            switch (quoteArgument.getType()) {
                case VARIABLE -> {
                    VariableArgument variableArgument = (VariableArgument) quoteArgument;
                    Variable argumentVariable = variableArgument.getVariable();

                    // create assignment: targetVariable <- sourceVariable
                    targetList.add(
                            new AssignmentInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable, labelForThisInstruction, argumentVariable, this, instructionNumber++));
                }

                case FUNCTION -> {
                    FunctionArgument functionArgument = (FunctionArgument) quoteArgument;
                    String innerFunctionName = functionArgument.getFunction().getName();
                    String innerFunctionArgumentsStr = functionArgument.getArgumentStr();

                    // create quote instruction: targetVariable <- (functionName, functionArguments...)
                    targetList.add(
                            new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable, labelForThisInstruction, this, instructionNumber++, innerFunctionName, innerFunctionArgumentsStr));
                }
            }

            indexOfQuoteArgumentList++;
        }

        return instructionNumber;
    }

    // Clone function instructions with remapped variables and labels
    private int addClonedFunctionInstructions(List<Instruction> targetList, int instructionNumber) {
        for (Instruction functionInstruction : getFunctionOfThisInstruction().getInstructionsList()) {
            Instruction cloned = functionInstruction.remapAndClone(
                    instructionNumber++,
                    mapFunctionToProgramVariable,
                    mapFunctionToProgramLabel,
                    this
            );

            targetList.add(cloned);
        }
        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable()); // TODO: check
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, origin, newInstructionNumber, this.functionName, this.functionArgumentsStrNotTrimmed);
    }

    // Assign function result back to the target of this Quote
    private void addResultAssignment(List<Instruction> targetList, int instructionNumber) {
        Variable mappedResult = mapFunctionToProgramVariable.get(Variable.RESULT);

        if (mappedResult == null) {
            throw new IllegalStateException("Function result variable not mapped correctly");
        }

        Label lastLabel = mapFunctionToProgramLabel.getOrDefault(FixedLabel.EXIT,  FixedLabel.EMPTY);

        targetList.add(
                new AssignmentInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), lastLabel, mappedResult, this, instructionNumber));
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
