package instruction.synthetic.quoteArguments;

import program.Program;
import variable.Variable;

public class VariableArgument extends QuoteArgument {
    private final Variable variable;
    private final String variableStr;

    public VariableArgument(Program parentProgram, String variableStr) {
        if (parentProgram == null) {
            throw new IllegalArgumentException("Program cannot be null");
        }
        if (variableStr == null || variableStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }

        // Try to find the variable by name
        Variable foundVariable = parentProgram.findVariableByName(variableStr.trim());

        if (foundVariable == null) {
            throw new IllegalArgumentException(
                    "Variable '" + variableStr + "' not found in program '" + parentProgram.getName() + "'"
            );
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
}