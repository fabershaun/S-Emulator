package instruction.synthetic.quoteArguments;

import execution.ExecutionContext;
import execution.ProgramExecutor;
import program.Program;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;

public class VariableArgument extends QuoteArgument {
    private final Variable variable;
    private final String variableStr;

    public VariableArgument(Program mainProgram, Program parentProgram, String variableStr) {
        if (parentProgram == null) {
            throw new IllegalArgumentException("Program cannot be null");
        }
        if (variableStr == null || variableStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }


        // Try to find the variable by name
        Variable foundVariable = parentProgram.findVariableByName(variableStr.trim());

        // Create variable
        if (foundVariable == null) {
            if (variableStr.startsWith("x")) {
                int number = Integer.parseInt(variableStr.substring(1));  // Cut the 'x'
                foundVariable = new VariableImpl(VariableType.INPUT, number);
            } else if (variableStr.startsWith("z")) {
                int number = Integer.parseInt(variableStr.substring(1));  // Cut the 'z'
                foundVariable = new VariableImpl(VariableType.WORK, number);
            }
        }

        if (foundVariable.getType().equals(VariableType.INPUT)) {
            if (!mainProgram.getInputVariables().contains(foundVariable)) {
                mainProgram.addInputVariable(foundVariable);
            }
        }



        this.variable = foundVariable;
        this.variableStr = variableStr;
    }

    @Override
    public ArgumentType getType() {
        return ArgumentType.VARIABLE;
    }

    public Variable getVariable() {
        return this.variable;
    }

    @Override
    public String getArgumentStr() {
        return this.variableStr;
    }

    public long getInputValueFromContext(ExecutionContext context) {
        return context.getVariableValue(this.variable);
    }
}
