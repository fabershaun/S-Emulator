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

    public VariableArgument(Variable variable) {
        this.variable = variable;
        this.variableStr = variable.getRepresentation();
    }

    public VariableArgument(String variableStr) {
        this.variableStr = variableStr;

        // Create variable
        if (variableStr.startsWith("x")) {
            int number = Integer.parseInt(variableStr.substring(1));  // Cut the 'x'
            this.variable = new VariableImpl(VariableType.INPUT, number);
        } else if (variableStr.startsWith("z")) {
            int number = Integer.parseInt(variableStr.substring(1));  // Cut the 'z'
            this.variable = new VariableImpl(VariableType.WORK, number);
        } else {
            throw new IllegalArgumentException("In VariableArgument constructor: variableStr must start with x or z");
        }
    }

    @Override
    public ArgumentType getType() {
        return ArgumentType.VARIABLE;
    }

    public Variable getVariable() {
        return this.variable;
    }

    public String getVariableStr() {
        return this.variableStr;
    }

    public long getInputValueFromContext(ExecutionContext context) {
        return context.getVariableValue(this.variable);
    }
}
