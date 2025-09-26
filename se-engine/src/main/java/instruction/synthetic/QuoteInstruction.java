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


import static instruction.synthetic.functionInstructionsUtils.FunctionInstructionUtils.*;
import static java.lang.Math.max;


public class QuoteInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final String functionName;
    private final List<QuoteArgument> quoteArguments =  new ArrayList<>();

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Map<Variable, Variable> variableMapping = new LinkedHashMap<>();
    private final Map<Label, Label> mapFunctionToProgramLabel = new HashMap<>();
    private int currentCyclesNumber;

    public QuoteInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Label label, Instruction origin, int instructionNumber, String functionName, List<QuoteArgument> quoteArguments) {
        super(mainProgram, programOfThisInstruction, InstructionData.QUOTATION, InstructionType.SYNTHETIC ,targetVariable, label, origin, instructionNumber);
        this.functionName = functionName;
        this.quoteArguments.addAll(quoteArguments);
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber, this.functionName, this.quoteArguments);
    }

    @Override
    public Label execute(ExecutionContext context) {
        ProgramExecutor functionExecutor = new ProgramExecutorImpl(this.getFunctionOfThisInstruction());
        List<Long> inputs = getInputs(quoteArguments, context, getMainProgram());

        // Run
        functionExecutor.run(0, inputs.toArray(Long[]::new));

        // Update value in parent program
        Variable resultVariable = this.getFunctionOfThisInstruction().getResultVariable();
        long quoteFunctionResult = functionExecutor.getVariableValue(resultVariable);
        context.updateVariable(getTargetVariable(), quoteFunctionResult);

        // Update cycles number
        currentCyclesNumber = InstructionData.QUOTATION.getCycles() + functionExecutor.getTotalCyclesOfProgram();

        return FixedLabel.EMPTY;
    }

//    private List<Long> getInputs(List<QuoteArgument> innerQuoteArgumentsList, ExecutionContext context) {
//        List<Long> inputs = new ArrayList<>();
//
//        for(QuoteArgument quoteFunctionArgument : innerQuoteArgumentsList) {
//            switch (quoteFunctionArgument.getType()) {
//                case FUNCTION -> {
//                    FunctionArgument functionArgument = (FunctionArgument) quoteFunctionArgument;
//                    long functionResult = calculateFunctionResult(functionArgument, context);
//                    inputs.add(functionResult);
//                }
//                case VARIABLE -> {
//                    VariableArgument innerVariableArgument = (VariableArgument) quoteFunctionArgument;
//                    long inputValue = 0;
//                    inputValue = innerVariableArgument.getInputValueFromContext(context);       // If it's INPUT variable
//
//                    inputs.add(inputValue);
//                }
//            }
//        }
//
//        return inputs;
//    }
//
//    // Recursive function: the goal is to reach to a functions that hold only variable arguments
//    private long calculateFunctionResult(FunctionArgument innerFunctionArgument, ExecutionContext context) {
//        String innerFunctionName = innerFunctionArgument.getFunctionName();
//        Program innerFunction = getMainProgram().getFunctionsHolder().getFunctionByName(innerFunctionName);
//
//        ProgramExecutor functionExecutor = new ProgramExecutorImpl(innerFunction);
//        List<Long> inputs = getInputs(innerFunctionArgument.getArguments(), context);
//
//        // Run
//        functionExecutor.run(0, inputs.toArray(Long[]::new));
//
//        // Return function result
//        Variable resultVariable = innerFunction.getResultVariable();
//        return functionExecutor.getVariableValue(resultVariable);
//    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String userString = getFunctionOfThisInstruction().getUserString();
        String arguments = buildCommandArguments(getMainProgram().getFunctionsHolder(), quoteArguments, variableMapping);

        StringBuilder command = new StringBuilder();
        command.append(targetVariableRepresentation);
        command.append(" <- ");
        command.append("(");
        command.append(userString);

        if (!arguments.isEmpty()) {
            command.append(",");
        }

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

        return 13;

//        DON'T DO IT LIKE BELLOW:
//        int maxDegree = 0;
//        this.setInnerInstructionsAndReturnTheNextOne(0);
//        for(Instruction instruction : this.innerInstructions) {
//            if(instruction instanceof SyntheticInstruction syntheticInstruction) {
//                maxDegree = max(maxDegree, syntheticInstruction.getMaxDegree());
//            }
//        }
//
//        return maxDegree + 1;
    }

    // todo: check if right
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

        int instructionNumber = startNumber;
        instructionNumber = addParameterInstructions(expandedInstructions, instructionNumber);       // Step 1: assign arguments to function input variables
        instructionNumber = addClonedFunctionInstructions(expandedInstructions, instructionNumber);  // Step 2: clone function instructions with variable/label remapping
        addResultAssignment(expandedInstructions, instructionNumber);

        return expandedInstructions;
    }

    // TODO: fix the generate problem of producing to many arguments
    private void mapQuoteFunctionVariables() {

        Variable functionResult = getFunctionOfThisInstruction().getResultVariable();
        Variable newWorkVariable = super.getMainProgram().generateUniqueVariable();
        variableMapping.put(functionResult , newWorkVariable);

        for (Variable functionInputVariable : getFunctionOfThisInstruction().getInputVariables()) {
            newWorkVariable = super.getMainProgram().generateUniqueVariable();
            variableMapping.put(functionInputVariable, newWorkVariable);
        }

        for (Variable functionWorkVariable : getFunctionOfThisInstruction().getWorkVariables()) {
            newWorkVariable = super.getMainProgram().generateUniqueVariable();
            variableMapping.put(functionWorkVariable, newWorkVariable);
        }
    }

    private void mapQuoteFunctionLabels() {
        Set<Label> labelsInFunction = new HashSet<>();
        labelsInFunction.addAll(getFunctionOfThisInstruction().getLabelsInProgram());
        labelsInFunction.addAll(getFunctionOfThisInstruction().getReferenceLabelsInProgram());

        // Map each function label to a unique label in the caller program
        for (Label functionLabel : labelsInFunction) {              // To include Exit if exist
            Label newLabel = super.getMainProgram().generateUniqueLabel();
            mapFunctionToProgramLabel.put(functionLabel, newLabel);
        }
    }

    private int addParameterInstructions(List<Instruction> targetList, int instructionNumber) {
        Label originalLabel = getLabel();
        boolean firstAssignment = true;

        int indexOfQuoteArgumentList = 0;
        Set<Variable> functionInputVariablesSet = getFunctionOfThisInstruction().getInputVariables();
        for (Variable inputVariable : functionInputVariablesSet) {

            Label labelForThisInstruction = (firstAssignment && (originalLabel != null) && (originalLabel != FixedLabel.EMPTY))     // To put the first label
                    ? originalLabel
                    : FixedLabel.EMPTY;
            firstAssignment = false;

            QuoteArgument quoteArgument = quoteArguments.get(indexOfQuoteArgumentList);
            Variable workVariable = variableMapping.get(inputVariable);

            switch (quoteArgument.getType()) {
                case VARIABLE -> {
                    VariableArgument variableArgument = (VariableArgument) quoteArgument;
                    Variable argumentVariable = variableArgument.getVariable();

                    // create assignment: targetVariable <- sourceVariable
                    targetList.add(                                                                  //mapped variable                      //original inner variable
                            new AssignmentInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable, labelForThisInstruction, argumentVariable, this, instructionNumber++));
                }

                case FUNCTION -> {
                    FunctionArgument functionArgument = (FunctionArgument) quoteArgument;
                    String innerFunctionName = functionArgument.getFunctionName();
                    List<QuoteArgument> convertedQuoteArgumentList = mapFunctionArgumentsToNewList(functionArgument.getArguments(), variableMapping, false);

                    // create quote instruction: targetVariable <- (functionName, functionArguments...)
                    targetList.add(                                                             //mapped variable
                            new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable, labelForThisInstruction, this, instructionNumber++, innerFunctionName, convertedQuoteArgumentList));
                }
            }

            indexOfQuoteArgumentList++;
        }

        return instructionNumber;
    }

    private int addClonedFunctionInstructions(List<Instruction> targetList, int instructionNumber) {
        for (Instruction functionInstruction : getFunctionOfThisInstruction().getInstructionsList()) {
            Instruction cloned = functionInstruction.remapAndClone(
                    instructionNumber++,
                    variableMapping,
                    mapFunctionToProgramLabel,
                    this,
                    getMainProgram()
            );

            targetList.add(cloned);
        }
        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction newOrigin, Program newMainProgram) {
        setMainProgram(newMainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, newOrigin, newInstructionNumber, this.functionName, quoteArguments);
    }

    // Assign function result back to the target of this Quote
    private void addResultAssignment(List<Instruction> targetList, int instructionNumber) {
        Variable mappedResult = variableMapping.get(Variable.RESULT);

        if (mappedResult == null) {
            throw new IllegalStateException("Function result variable not mapped correctly");
        }

        Label lastLabel = mapFunctionToProgramLabel.getOrDefault(FixedLabel.EXIT,  FixedLabel.EMPTY);

        targetList.add(
                new AssignmentInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), lastLabel, mappedResult, this, instructionNumber));
    }
}
