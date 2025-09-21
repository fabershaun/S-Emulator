package instruction.synthetic.quoteArguments;

import program.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionArgument extends QuoteArgument {
    private final String rawFunctionStr;
    private final Program mainProgram;
    private final Program parentProgram;
    private Program function;
    private final List<QuoteArgument> arguments = new ArrayList<>();
    private String rawInnerArgumentsString;

    public FunctionArgument(Program mainProgram, Program parentProgram, String functionStrNotTrimmed) {
        if (parentProgram == null && mainProgram == null) {
            throw new IllegalArgumentException("Program cannot be null");
        }
        if (functionStrNotTrimmed == null || functionStrNotTrimmed.trim().isEmpty()) {
            throw new IllegalArgumentException("Function data cannot be null or empty");
        }

        this.mainProgram = mainProgram;
        this.parentProgram = parentProgram;
        this.rawFunctionStr = functionStrNotTrimmed.trim();
        extractFunctionDataFromStr();
    }

    private void extractFunctionDataFromStr() {
        List<String> functionData = splitDataToStrList();
        String functionName = functionData.getFirst();

        this.function = parentProgram.getFunctionsHolder().getFunctionByName(functionName);
        if (this.function == null) {
            throw new IllegalArgumentException("Function '" + functionName + "' not found in program '" + parentProgram.getName() + "'");
        }

        setArgumentsList(functionData);
        setRawArgumentsString();
    }

    private List<String> splitDataToStrList() {
        if (rawFunctionStr == null || rawFunctionStr.trim().isEmpty()) {
            throw new IllegalArgumentException("In FunctionArgument class: functionStrNotTrimmed is null or empty");
        }

        String functionStrTrimmed = rawFunctionStr.trim();
        if(!functionStrTrimmed.startsWith("(") || !functionStrTrimmed.endsWith(")")) {
            throw new IllegalArgumentException("In FunctionArgument class: functionStrNotTrimmed is invalid format");
        }

        functionStrTrimmed = functionStrTrimmed.substring(1, functionStrTrimmed.length() - 1);

        List<String> functionData = new ArrayList<>();
        StringBuilder data = new StringBuilder();
        int parenthesesDepth = 0;

        for (char c : functionStrTrimmed.toCharArray()) {
            if (c == ',' && parenthesesDepth == 0) {
                functionData.add(data.toString().trim());
                data.setLength(0);
            } else {
                if (c == '(') parenthesesDepth++;
                if (c == ')') parenthesesDepth--;
                data.append(c);
            }
        }

        if (!data.isEmpty()) {                   // To add the last part or to add the argument if there were no parentheses in the original string
            functionData.add(data.toString().trim());
        }

        return functionData;
    }

    private void setArgumentsList(List<String> functionData) {
        for(int i = 1; i < functionData.size(); i++) {  // Skip function name that in index 0
            String argumentStr = functionData.get(i);
            if (argumentStr.startsWith("(") && argumentStr.endsWith(")")) {
                arguments.add(new FunctionArgument(mainProgram, parentProgram, argumentStr));
            } else {
                arguments.add(new VariableArgument(mainProgram, parentProgram, argumentStr));
            }
        }
    }

    private void setRawArgumentsString() {
        this.rawInnerArgumentsString = this.arguments.stream()
                .map(QuoteArgument::getArgumentStr)
                .collect(Collectors.joining(","));
    }

    @Override
    public ArgumentType getType() {
        return ArgumentType.FUNCTION;
    }

    public Program getFunction() {
        return function;
    }

    public List<QuoteArgument> getArguments() {
        return arguments;
    }

    @Override
    public String getArgumentStr() {
        return this.rawInnerArgumentsString;
    }
}