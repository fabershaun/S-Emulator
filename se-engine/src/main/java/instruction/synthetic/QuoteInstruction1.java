package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteInstruction1 extends AbstractInstruction implements SyntheticInstruction {
    private final String functionName;
    private final String functionArguments;
    private int maxDegree;
    private int currentCyclesNumber;
    private boolean expanded = false;
    private Program function;        // To know the labels and variables that the function used
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Map<Label, Instruction> labelToInnerInstruction = new HashMap<>();
    private final Map<Variable, Variable> functionToProgramVariable = new HashMap<>();
    private final Map<Label, Label> functionToProgramLabel = new HashMap<>();


    public QuoteInstruction1(Variable targetVariable, Label label, Instruction origin, int instructionNumber, String functionName, String functionArguments) {
        super(InstructionData.QUOTATION, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.functionName = functionName;
        this.functionArguments = functionArguments;
        currentCyclesNumber = InstructionData.QUOTATION.getCycles();
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new QuoteInstruction1(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber, functionName, functionArguments);
    }

    // already copied
    public void setFunctionForQuoteInstruction() {
//        if (super.getProgramOfThisInstruction() instanceof ProgramImpl mainProgram) {
//            this.function = mainProgram.getFunctions()
//                    .stream()
//                    .filter(arg -> arg.getName().equalsIgnoreCase(this.functionName))
//                    .findFirst()
//                    .orElseThrow(() -> new IllegalArgumentException("Function not found: " + functionName));
//        }
    }

    // DONT USED
    private void resetNumberOfCycles() {
        this.currentCyclesNumber = InstructionData.QUOTATION.getCycles();
    }

    @Override
    public int getCycleOfInstruction() {
        return this.currentCyclesNumber;
    }

    //Changed
    @Override
    public Label execute(ExecutionContext context) {
        resetNumberOfCycles();
        if (!expanded) {
            this.innerInstructions.clear();
            setInnerInstructionsAndReturnTheNextOne(1);
        }

        Instruction currentInstruction = innerInstructions.get(0);
        Instruction nextInstruction = null;
        Label nextLabel;

        do {
            nextLabel = currentInstruction.execute(context);
            currentCyclesNumber += currentInstruction.getCycleOfInstruction();

            if (nextLabel == FixedLabel.EMPTY) {
                int indexOfNextInstruction = innerInstructions.indexOf(currentInstruction) + 1;
                if (indexOfNextInstruction < innerInstructions.size()) {
                    nextInstruction = innerInstructions.get(indexOfNextInstruction);
                } else {
                    nextLabel = FixedLabel.EXIT;
                }
            } else if (nextLabel != FixedLabel.EXIT) {
                nextInstruction = labelToInnerInstruction.get(nextLabel);
                if (nextInstruction == null) {
                    throw new IllegalStateException("Label not found: " + nextLabel);
                }
            }

            currentInstruction = nextInstruction;
        } while (nextLabel != FixedLabel.EXIT);

        return FixedLabel.EMPTY;
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

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        if (expanded) {
            return startNumber + innerInstructions.size();  // Already expanded, dont need again
        }

        innerInstructions.clear();
        List<Instruction> functionInstructionConverted = convertFunctionData(startNumber);
        innerInstructions.addAll(functionInstructionConverted);

        this.maxDegree = calculateMaxDegree();
        this.expanded = true;
        return startNumber + innerInstructions.size();
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap) {
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new QuoteInstruction1(newTargetVariable, newLabel, this.getOriginalInstruction(), newInstructionNumber, this.functionName, this.functionArguments);
    }

    private List<Instruction> convertFunctionData(int startNumber) {
        List<Instruction> functionInstructionConverted = new ArrayList<>();

        mapFunctionVariables();
        mapFunctionLabels();

        int instructionNumber = startNumber;

        // לקבל את הארגומנטים לרשימות ולהפריד בין ארגומנטים שהם משתנים לבין פונקציות ולשלוח לפונקציות עזר בהתאם

        instructionNumber = addParameterAssignments(functionInstructionConverted, instructionNumber);       // Step 1: assign arguments to function input variables
        instructionNumber = addClonedFunctionInstructions(functionInstructionConverted, instructionNumber); // Step 2: clone function instructions with variable/label remapping
        instructionNumber = addResultAssignment(functionInstructionConverted, instructionNumber);

        labelToInnerInstruction.clear();
        for (Instruction instr : functionInstructionConverted) {
            if (instr.getLabel() != null && instr.getLabel() != FixedLabel.EMPTY) {
                labelToInnerInstruction.put(instr.getLabel(), instr);
            }
        }

        return functionInstructionConverted;
    }

    private int addParameterAssignments(List<Instruction> targetList, int instructionNumber) {
        List<String> argumentsNames = parseCallerArgNames();
        List<Variable> functionInputs = function.getInputVariables().stream().toList();

        Label originalLabel = getLabel();
        boolean firstAssignment = true;

        for (int i = 0; i < Math.min(argumentsNames.size(), functionInputs.size()); i++) {
            Variable functionVariable = functionInputs.get(i);
            Variable mappedVariable = functionToProgramVariable.get(functionVariable);

            // find the caller variable (by name from args[i])
            Variable callerVariable = super.getProgramOfThisInstruction().findVariableByName(argumentsNames.get(i));

            Label labelForThisInstruction = firstAssignment && originalLabel != null && originalLabel != FixedLabel.EMPTY
                    ? originalLabel
                    : FixedLabel.EMPTY;
            firstAssignment = false;

            // create assignment: mappedVariable <- callerVariable
            targetList.add(
                    new AssignmentInstruction(mappedVariable, labelForThisInstruction, callerVariable, this, instructionNumber++));
        }

        return instructionNumber;
    }

    // NOT USED
    private List<String> parseCallerArgNames() {
        String argsStr = (functionArguments == null) ? "" : functionArguments.trim();
        if (argsStr.isEmpty()) return List.of();

        // Remove surrounding brackets if any
        if (argsStr.startsWith("(") && argsStr.endsWith(")")) {
            argsStr = argsStr.substring(1, argsStr.length() - 1).trim();
        }

        // Split by commas, clean up stray spaces and parentheses
        List<String> tokens = new ArrayList<>();
        for (String raw : argsStr.split(",")) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) continue;
            tokens.add(trimmed);
        }
        if (tokens.isEmpty()) return List.of();

        return tokens;
    }

    // Clones function instructions into the caller program with remapped variables and labels
    private int addClonedFunctionInstructions(List<Instruction> targetList, int instructionNumber) {
        for (Instruction functionInstruction : function.getInstructionsList()) {
            Instruction cloned = functionInstruction.remapAndClone(
                    instructionNumber++,
                    functionToProgramVariable,
                    functionToProgramLabel
            );

            targetList.add(cloned);
        }
        return instructionNumber;
    }

    // Assign function result back to the target of this Quote
    private int addResultAssignment(List<Instruction> targetList, int instructionNumber) {
        Variable callerTarget = getTargetVariable();
        Variable mappedResult = functionToProgramVariable.get(Variable.RESULT);

        if (mappedResult == null) {
            throw new IllegalStateException("Function result variable not mapped correctly");
        }


        targetList.add(
                new AssignmentInstruction(callerTarget, mappedResult, this, instructionNumber++)
        );

        return instructionNumber;
    }

    private void mapFunctionLabels() {
        // Map each function label to a unique label in the caller program
        for (Label functionLabel : function.getLabelsInProgram()) {
            Label newLabel = super.getProgramOfThisInstruction().generateUniqueLabel();
            functionToProgramLabel.put(functionLabel, newLabel);
        }
    }

    private void mapFunctionVariables() {
        Program mainProgram = super.getProgramOfThisInstruction();

        for (Variable functionInputVariable : function.getInputVariables()) {
            Variable newWorkVariable = mainProgram.generateUniqueVariable();
            functionToProgramVariable.put(functionInputVariable, newWorkVariable);
        }

        for (Variable functionWorkVariable : function.getWorkVariables()) {
            Variable newWorkVariable = mainProgram.generateUniqueVariable();
            functionToProgramVariable.put(functionWorkVariable, newWorkVariable);
        }

        // Variable functionResult = Variable.RESULT;
        Variable functionResult = function.getResultVariable();
        Variable newWorkVariable = mainProgram.generateUniqueVariable();
        functionToProgramVariable.put(functionResult , newWorkVariable);

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
