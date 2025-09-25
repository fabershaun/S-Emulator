package instruction.synthetic.functionInstructionsUtils;

import instruction.synthetic.quoteArguments.FunctionArgument;
import instruction.synthetic.quoteArguments.QuoteArgument;
import instruction.synthetic.quoteArguments.VariableArgument;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionInstructionUtils {
    public static String buildCommandArgumentsStrict(List<QuoteArgument> arguments, Map<Variable, Variable> variableMapping) {

        return arguments.stream()
                .map(argument -> buildSingleArgumentStrict(argument, variableMapping))
                .collect(Collectors.joining(","));
    }

    private static String buildSingleArgumentStrict(QuoteArgument argument, Map<Variable, Variable> variableMapping) {

        return switch (argument) {
            case VariableArgument variableArgument -> {
                Variable innerVariable = variableArgument.getVariable();
                Variable mappedVariable = variableMapping.get(innerVariable);

                if (mappedVariable == null) {
                    throw new IllegalArgumentException(
                            "Variable not found in mapping: " + innerVariable.getRepresentation()
                    );
                }

                yield mappedVariable.getRepresentation();
            }

            case FunctionArgument functionArgument -> {
                String innerArgumentsAsString = functionArgument.getArguments().stream()
                        .map(innerArgument -> buildSingleArgumentStrict(innerArgument, variableMapping))
                        .collect(Collectors.joining(","));
                yield "(" + functionArgument.getFunctionName()
                        + (innerArgumentsAsString.isEmpty() ? "" : "," + innerArgumentsAsString)
                        + ")";
            }

            default -> throw new IllegalStateException(
                    "Unsupported QuoteArgument type: " + argument.getClass()
            );
        };
    }

    public static List<QuoteArgument> mapFunctionArgumentsToNewList(List<QuoteArgument> arguments, Map<Variable, Variable> variableMapping) {

        List<QuoteArgument> mappedArguments = new ArrayList<>();

        for (QuoteArgument argument : arguments) {
            switch (argument) {
                case VariableArgument variableArgument -> {
                    Variable originalVariable = variableArgument.getVariable();
                    Variable mappedVariable = variableMapping.get(originalVariable);

                    if (mappedVariable == null) {
                        throw new IllegalArgumentException(
                                "In FunctionInstructionUtils: Variable not found in mapping: " + originalVariable.getRepresentation()
                        );
                    }

                    mappedArguments.add(new VariableArgument(mappedVariable));
                }

                case FunctionArgument functionArgument -> {
                    List<QuoteArgument> mappedInnerArguments =
                            mapFunctionArgumentsToNewList(functionArgument.getArguments(), variableMapping);

                    mappedArguments.add(new FunctionArgument(functionArgument.getFunctionName(), mappedInnerArguments));
                }

                default -> throw new IllegalStateException(
                        "In FunctionInstructionUtils: Unsupported QuoteArgument type: " + argument.getClass()
                );
            }
        }

        return mappedArguments;
    }
}
