package engine.logic.programData.instruction.synthetic;

import dto.v3.UserDTO;
import engine.logic.execution.ExecutionContext;
import engine.logic.execution.runMode.ProgramExecutor;
import engine.logic.execution.runMode.ProgramExecutorImpl;
import engine.logic.programData.instruction.*;
import engine.logic.programData.instruction.synthetic.functionInstructionsUtils.FunctionExecutionResult;
import engine.logic.programData.instruction.synthetic.quoteArguments.FunctionArgument;
import engine.logic.programData.instruction.synthetic.quoteArguments.QuoteArgument;
import engine.logic.programData.instruction.synthetic.quoteArguments.VariableArgument;
import engine.logic.programData.label.FixedLabel;
import engine.logic.programData.label.Label;
import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;
import java.util.*;

import static engine.logic.programData.instruction.synthetic.functionInstructionsUtils.FunctionExecutionResult.addFunctionArgumentCycles;
import static engine.logic.programData.instruction.synthetic.functionInstructionsUtils.FunctionExecutionResult.extractInputValues;
import static engine.logic.programData.instruction.synthetic.functionInstructionsUtils.FunctionInstructionUtils.*;


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
    public Label execute(ExecutionContext context, UserDTO userDTO) {
        ProgramExecutor functionExecutor = new ProgramExecutorImpl(this.getFunctionOfThisInstruction());
        List<FunctionExecutionResult> functionExecutionResultList = getInputs(quoteArguments, context, getMainProgram(), userDTO);

        // Run
        functionExecutor.run(userDTO, null, 0, extractInputValues(functionExecutionResultList));  // architectureTypeSelected - not needed here (it's an inner call)

        // Update value in parent program
        Variable resultVariable = this.getFunctionOfThisInstruction().getResultVariable();
        long quoteFunctionResult = functionExecutor.getVariableValue(resultVariable);
        context.updateVariable(getTargetVariable(), quoteFunctionResult);

        // Update cycles number
        currentCyclesNumber = InstructionData.QUOTATION.getCycles() + functionExecutor.getTotalCycles() + addFunctionArgumentCycles(functionExecutionResultList);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String userString = getFunctionOfThisInstruction().getUserString();
        String arguments = buildCommandArguments(getMainProgram().getProgramsHolder(), quoteArguments, variableMapping);

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
    public int expandInstruction(int startNumber) {
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
        return super.getMainProgram().getFunctionByName(this.functionName);
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
        List<QuoteArgument> mappedQuoteArguments = mapFunctionArgumentsToNewList(quoteArguments, variableMap, true);

        return new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, newOrigin, newInstructionNumber, this.functionName, mappedQuoteArguments);
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
