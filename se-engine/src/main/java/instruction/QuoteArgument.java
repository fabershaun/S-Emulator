package instruction;

import program.Program;
import variable.Variable;

public class QuoteArgument {

    public enum ArgumentType {
        VARIABLE,
        FUNCTION
    }

    private final ArgumentType type;
    private final Variable variable;
    private final Program function;

    // Private
    private QuoteArgument(ArgumentType type, Variable variable, Program function) {
        this.type = type;
        this.variable = variable;
        this.function = function;
    }

    // factory methods
    public static QuoteArgument fromVariable(Variable variable) {
        return new QuoteArgument(ArgumentType.VARIABLE, variable, null);
    }

    public static QuoteArgument fromFunction(Program function) {
        return new QuoteArgument(ArgumentType.FUNCTION, null, function);
    }

    // getters
    public ArgumentType getType() {
        return type;
    }

    public Variable getVariable() {
        if (type != ArgumentType.VARIABLE) {
            throw new IllegalStateException("Not a variable argument");
        }
        return variable;
    }

    public Program getFunction() {
        if (type != ArgumentType.FUNCTION) {
            throw new IllegalStateException("Not a function argument");
        }
        return function;
    }
}
