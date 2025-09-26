package instruction.synthetic.quoteArguments;

import java.util.ArrayList;
import java.util.List;

public class FunctionArgument extends QuoteArgument {
    private final String functionName;
    private final List<QuoteArgument> arguments = new ArrayList<>();

    public FunctionArgument(String functionName, List<QuoteArgument> arguments) {
        this.functionName = functionName;
        this.arguments.addAll(arguments);
    }

    @Override
    public ArgumentType getType() {
        return ArgumentType.FUNCTION;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<QuoteArgument> getArguments() {
        return arguments;
    }
}