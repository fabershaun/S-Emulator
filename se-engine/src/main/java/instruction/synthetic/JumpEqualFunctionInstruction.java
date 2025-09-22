package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.synthetic.quoteArguments.QuoteArgument;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;
import variable.VariableImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualFunctionInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final String referenceFunctionName;
    private final String referenceFunctionArguments;
    private final Label referenceLabel;
    private final List<Instruction> innerInstructions = new ArrayList<>();

    public JumpEqualFunctionInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Label label, Label referenceLabel, String referenceFunctionName, String referenceFunctionArguments, Instruction origin, int instructionNumber) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_EQUAL_FUNCTION, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.referenceFunctionName = referenceFunctionName;
        this.referenceFunctionArguments = referenceFunctionArguments;
        this.referenceLabel = referenceLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpEqualFunctionInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), referenceLabel, referenceFunctionName, referenceFunctionArguments, getOriginalInstruction(), instructionNumber);
    }

    // todo: fix
    @Override
    public Label execute(ExecutionContext context) {
        long targetVariableValue = context.getVariableValue(getTargetVariable());


        Instruction quoteInstruction = new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), ,)
        long functionResult = 0;

        return (targetVariableValue == functionResult) ? referenceLabel : FixedLabel.EMPTY;
    }

    // todo: fix
    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String userString = "";//getFunctionOfThisInstruction().getUserString();
        String arguments = "";//replaceFunctionsWithUserStringsStrict();
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

    // todo: combine with quote
    @Override
    public int getMaxDegree() {
        return 0;
        //return helperGetMaxDegree(getFunctionOfThisInstruction(), quoteArguments);
    }

    // todo: fix
    // Recursive function
    private int helperGetMaxDegree(Program innerFunction, List<QuoteArgument> innerQuoteArguments) {
        int maxDegreeOfQuoteFunction = innerFunction.calculateProgramMaxDegree();    // Calculate inner quote's function degree
        int maxDegreeOfQuoteFunctionArguments = 0; //calculateDegreeOfQuoteFunctionArguments(innerQuoteArguments); // Calculate inner quote's function's arguments degree

        return maxDegreeOfQuoteFunction + maxDegreeOfQuoteFunctionArguments;
    }

    @Override
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        Variable workVariable1 = super.getMainProgram().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        int instructionNumber = startNumber;

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
}

