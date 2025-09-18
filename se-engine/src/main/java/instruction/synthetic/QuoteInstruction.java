package instruction.synthetic;

import execution.ExecutionContext;
import execution.ProgramExecutor;
import execution.ProgramExecutorImpl;
import instruction.*;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;

import java.util.*;

public class QuoteInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final String functionName;
    private final String functionArgumentsStrNotTrimmed;
    //private Program functionInInstruction;        // To know the labels and variables that the function used
    private final List<QuoteArgument> quoteArguments =  new ArrayList<>();

    private ProgramExecutor programExecutor;
    private boolean initialized;

    private int currentCyclesNumber;
    private int maxDegree = 4; // Dynamic    //TODO

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Map<Label, Instruction> labelToInnerInstruction = new HashMap<>();
    private final Map<Variable, Variable> mapFunctionToProgramVariable = new HashMap<>();
    private final Map<Program, Variable> mapQuoteFunctionToFunctionVariable = new HashMap<>();
    private final Map<Label, Label> mapFunctionToProgramLabel = new HashMap<>();

    public QuoteInstruction(Variable targetVariable, Label label, Instruction origin, int instructionNumber, String functionName, String functionArgumentsStrNotTrimmed) {
        super(InstructionData.QUOTATION, InstructionType.SYNTHETIC ,targetVariable, label, origin, instructionNumber);
        this.functionName = functionName;
        this.functionArgumentsStrNotTrimmed = functionArgumentsStrNotTrimmed;
        this.initialized = false;
    }

//    public void initialize() {
//        extractQuoteArguments();
//        //this.functionInInstruction = super.getProgramOfThisInstruction().getFunctionsHolder().getFunctionByName(this.functionName);
//    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new QuoteInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber, functionName, functionArgumentsStrNotTrimmed);
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable()); // TODO: check
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new QuoteInstruction(newTargetVariable, newLabel, this.getOriginalInstruction(), newInstructionNumber, this.functionName, this.functionArgumentsStrNotTrimmed);
    }

    @Override
    public Label execute(ExecutionContext context) {
        Program functionInInstruction = this.getFunctionOfThisInstruction();
        ProgramExecutor functionExecutor = new ProgramExecutorImpl(functionInInstruction);

        Long[] inputs = new Long[0];
        functionExecutor.run(0, inputs);

        currentCyclesNumber = InstructionData.QUOTATION.getCycles() + functionExecutor.getTotalCyclesOfProgram();
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String userString = getFunctionOfThisInstruction().getUserString();
        StringBuilder command = new StringBuilder();

        command.append("(");
        command.append(targetVariableRepresentation);
        command.append(" <- ");
        command.append(userString);
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
        extractQuoteArguments();
        List<Instruction> expandedInstructions = convertFunctionData(startNumber);
        innerInstructions.clear();
        innerInstructions.addAll(expandedInstructions);

        return startNumber + innerInstructions.size();
    }

    @Override
    public int getCycleOfInstruction() {
        return this.currentCyclesNumber;
    }

    // todo: delete
//    public void setFunctionForQuoteInstruction() {
//        this.functionInInstruction = super.getProgramOfThisInstruction().getFunctionsHolder().getFunctionByName(functionInInstruction.getName());
//    }

    public String getFunctionNameOfQuoteInstruction() {
        return this.functionName;
    }

    public Program getFunctionOfThisInstruction() {
        return super.getProgramOfThisInstruction().getFunctionsHolder().getFunctionByName(this.functionName);
    }

    private List<Instruction> convertFunctionData(int startNumber) {
        List<Instruction> expandedInstructions = new ArrayList<>();

        mapFunctionVariables();
        mapFunctionLabels();
        mapFunctionVariableToProgramFunction();

        int instructionNumber = startNumber;

//        if (!initialized) {
//            initialize();
//            initialized = true;
//        }

        instructionNumber = addParameterInstructions(expandedInstructions, instructionNumber);       // Step 1: assign arguments to function input variables
        instructionNumber = addClonedFunctionInstructions(expandedInstructions, instructionNumber); // Step 2: clone function instructions with variable/label remapping
        addResultAssignment(expandedInstructions, instructionNumber);

        //labelToInnerInstruction.clear();    // To reset after each expand
        for (Instruction instruction : expandedInstructions) {
            if (instruction.getLabel() != null && instruction.getLabel() != FixedLabel.EMPTY) {
                labelToInnerInstruction.put(instruction.getLabel(), instruction);
            }
        }

        return expandedInstructions;
    }

    private void mapFunctionVariables() {
        Program mainProgram = super.getProgramOfThisInstruction();

        for (Variable functionInputVariable : getFunctionOfThisInstruction().getInputVariables()) {
            Variable newWorkVariable = mainProgram.generateUniqueVariable();
            mapFunctionToProgramVariable.put(functionInputVariable, newWorkVariable);
        }

        for (Variable functionWorkVariable : getFunctionOfThisInstruction().getWorkVariables()) {
            Variable newWorkVariable = mainProgram.generateUniqueVariable();
            mapFunctionToProgramVariable.put(functionWorkVariable, newWorkVariable);
        }

        // Variable functionResult = Variable.RESULT;
        Variable functionResult = getFunctionOfThisInstruction().getResultVariable();
        Variable newWorkVariable = mainProgram.generateUniqueVariable();
        mapFunctionToProgramVariable.put(functionResult , newWorkVariable);

    }

    private void mapFunctionLabels() {
        // Map each function label to a unique label in the caller program
        for (Label functionLabel : getFunctionOfThisInstruction().getLabelsInProgram()) {
            Label newLabel = super.getProgramOfThisInstruction().generateUniqueLabel();
            mapFunctionToProgramLabel.put(functionLabel, newLabel);
        }
    }

    private void mapFunctionVariableToProgramFunction() {
        Program mainProgram = super.getProgramOfThisInstruction();

        for(QuoteArgument quoteArgument : quoteArguments) {
            if (quoteArgument.getType().equals(QuoteArgument.ArgumentType.FUNCTION)) {
                Variable newWorkVariable = mainProgram.generateUniqueVariable();
                mapQuoteFunctionToFunctionVariable.put(quoteArgument.getFunction(), newWorkVariable);
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

            if(quoteArgument.getType().equals(QuoteArgument.ArgumentType.VARIABLE)) {
                Variable sourceVariable = quoteArgument.getVariable();                      // sourceVariable = original Quote instruction input variable
                Variable targetVariable = mapFunctionToProgramVariable.get(sourceVariable);    // targetVariable = new work variable that we created

                // create assignment: targetVariable <- sourceVariable
                targetList.add(
                        new AssignmentInstruction(targetVariable, labelForThisInstruction, sourceVariable, getOriginalInstruction(), instructionNumber++));
            }
            else if(quoteArgument.getType().equals(QuoteArgument.ArgumentType.FUNCTION)) {
                Program functionInArguments = quoteArgument.getFunction();
                Variable targetVariable = mapQuoteFunctionToFunctionVariable.get(functionInArguments);    // targetVariable = new work variable that we created
                String newFunctionName = functionInArguments.getName();
                String newFunctionsArgumentsStr = "";

                List<Instruction> instructions = functionInArguments.getInstructionsList();
                for(Instruction instruction : instructions) {
                    if(instruction instanceof QuoteInstruction quoteInstruction && quoteInstruction.getFunctionNameOfQuoteInstruction().equals(newFunctionName)) {
                        newFunctionsArgumentsStr = quoteInstruction.getFunctionNameOfQuoteInstruction();
                    }
                }
                // create assignment: targetVariable <- functioName
                targetList.add(
                        new QuoteInstruction(targetVariable, labelForThisInstruction, getOriginalInstruction(), instructionNumber++, newFunctionName, newFunctionsArgumentsStr));
            }
        }

        return instructionNumber;
    }

    // Clones function instructions into the caller program with remapped variables and labels
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

    // Assign function result back to the target of this Quote
    private void addResultAssignment(List<Instruction> targetList, int instructionNumber) {
        Variable mappedResult = mapFunctionToProgramVariable.get(Variable.RESULT);

        if (mappedResult == null) {
            throw new IllegalStateException("Function result variable not mapped correctly");
        }

        Label lastLabel = mapFunctionToProgramLabel.getOrDefault(FixedLabel.EXIT,  FixedLabel.EMPTY);

        targetList.add(
                new AssignmentInstruction(getTargetVariable(), lastLabel, mappedResult, getOriginalInstruction(), instructionNumber));
    }

    private void extractQuoteArguments() {
        if (functionArgumentsStrNotTrimmed == null || functionArgumentsStrNotTrimmed.trim().isEmpty()) {
            return;
        }

        String argsStr = functionArgumentsStrNotTrimmed.trim();
        if (argsStr.startsWith("(") && argsStr.endsWith(")")) {
            argsStr = argsStr.substring(1, argsStr.length() - 1).trim();
        }

        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenDepth = 0;

        for (char c : argsStr.toCharArray()) {
            if (c == ',' && parenDepth == 0) {
                tokens.add(current.toString().trim());
                current.setLength(0);
            } else {
                if (c == '(') parenDepth++;
                if (c == ')') parenDepth--;
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            tokens.add(current.toString().trim());
        }

        Set<String> definedFunctions = super.getProgramOfThisInstruction().getFunctionsHolder().getFunctionNamesUpperCase();
        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (token.contains("(") || definedFunctions.contains(token.toUpperCase(Locale.ROOT))) {  // If token is a Function:
                String innerFunctionName = getFunctionNameFromToken(token);
                String innerFunctionUserString = "";

                Program innerFunction = super.getProgramOfThisInstruction().getFunctionsHolder().getFunctionByName(innerFunctionName);
                quoteArguments.add(QuoteArgument.fromFunction(innerFunction));
            }
            else {    // If token is a Variable:
                Program function = getFunctionOfThisInstruction();
                Variable variable = function.findVariableByName(token);

                //Variable variable = getFunctionOfThisInstruction().findVariableByName(token);
                if (variable == null) {
                    throw new IllegalArgumentException("Variable not found: " + token);
                }
                quoteArguments.add(QuoteArgument.fromVariable(variable));
            }
        }
    }

    private String getFunctionNameFromToken(String token) {

        int indexOfOpenParen = token.indexOf("(");
        String afterParen = token.substring(indexOfOpenParen + 1).trim();

        int endIndex = afterParen.indexOf(',');
        int closeIndex = afterParen.indexOf(')');

        if (endIndex == -1 || (closeIndex != -1 && closeIndex < endIndex)) {
            endIndex = closeIndex;
        }

        return (endIndex == -1 ? afterParen : afterParen.substring(0, endIndex)).trim();
    }
}
