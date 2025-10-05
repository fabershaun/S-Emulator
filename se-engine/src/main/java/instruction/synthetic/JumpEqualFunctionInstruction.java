package instruction.synthetic;

import execution.ExecutionContext;
import execution.ProgramExecutor;
import execution.ProgramExecutorImpl;
import instruction.*;
import instruction.synthetic.functionInstructionsUtils.FunctionExecutionResult;
import instruction.synthetic.quoteArguments.QuoteArgument;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static instruction.synthetic.functionInstructionsUtils.FunctionExecutionResult.addFunctionArgumentCycles;
import static instruction.synthetic.functionInstructionsUtils.FunctionExecutionResult.extractInputValues;
import static instruction.synthetic.functionInstructionsUtils.FunctionInstructionUtils.buildCommandArguments;
import static instruction.synthetic.functionInstructionsUtils.FunctionInstructionUtils.getInputs;

public class JumpEqualFunctionInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final String referenceFunctionName;
    private final List<QuoteArgument> quoteArguments =  new ArrayList<>();
    private final Label referenceLabel;

    private int currentCyclesNumber;
    private final List<Instruction> innerInstructions = new ArrayList<>();

    public JumpEqualFunctionInstruction(Program mainProgram, Program programOfThisInstruction, Variable targetVariable, Label label, Label referenceLabel, Instruction origin, int instructionNumber, String referenceFunctionName, List<QuoteArgument> quoteArguments) {
        super(mainProgram, programOfThisInstruction, InstructionData.JUMP_EQUAL_FUNCTION, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.referenceFunctionName = referenceFunctionName;
        this.quoteArguments.addAll(quoteArguments);
        this.referenceLabel = referenceLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpEqualFunctionInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), getLabel(), referenceLabel, getOriginalInstruction(), instructionNumber, this.referenceFunctionName, this.quoteArguments);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long targetVariableValue = context.getVariableValue(getTargetVariable());

        ProgramExecutor functionExecutor = new ProgramExecutorImpl(this.getFunctionOfThisInstruction());
        List<FunctionExecutionResult> functionExecutionResultList = getInputs(quoteArguments, context, getMainProgram());

        // Run
        functionExecutor.run(0, extractInputValues(functionExecutionResultList));

        // Update value in parent program
        Variable resultVariable = this.getFunctionOfThisInstruction().getResultVariable();
        long functionResult = functionExecutor.getVariableValue(resultVariable);

        // Update cycles number
        currentCyclesNumber = InstructionData.JUMP_EQUAL_FUNCTION.getCycles() + functionExecutor.getTotalCycles() + addFunctionArgumentCycles(functionExecutionResultList);

        return (targetVariableValue == functionResult) ? referenceLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String userString = getFunctionOfThisInstruction().getUserString();

        String arguments = buildCommandArguments(getMainProgram().getProgramsHolder(), quoteArguments, Map.of());
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(targetVariableRepresentation);
        command.append(" = ");
        command.append("(");
        command.append(userString);

        if (!arguments.isEmpty()) {
            command.append(",");
        }

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
    public int expandInstruction(int startNumber) {
        Variable workVariable1 = super.getMainProgram().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        int instructionNumber = startNumber;

        innerInstructions.clear();
        innerInstructions.add(new QuoteInstruction(getMainProgram(), getProgramOfThisInstruction(), workVariable1, newLabel1, getOriginalInstruction(), instructionNumber++, referenceFunctionName, quoteArguments));
        innerInstructions.add(new JumpEqualVariableInstruction(getMainProgram(), getProgramOfThisInstruction(), getTargetVariable(), workVariable1, referenceLabel, getOriginalInstruction(), instructionNumber++));
        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction newOrigin, Program newMainProgram) {
        setMainProgram(newMainProgram);
        Variable newTargetVariable = variableMap.getOrDefault(this.getTargetVariable(), this.getTargetVariable());
        Label newLabel = labelMap.getOrDefault(this.getLabel(), this.getLabel());

        return new JumpEqualFunctionInstruction(getMainProgram(), getProgramOfThisInstruction(), newTargetVariable, newLabel, referenceLabel, newOrigin, newInstructionNumber, referenceFunctionName, quoteArguments);
    }

    public Program getFunctionOfThisInstruction() {
        return super.getMainProgram().getFunctionByName(this.referenceFunctionName);
    }
}

