package instruction.synthetic.functionInstructionsUtils;

import instruction.synthetic.quoteArguments.FunctionArgument;
import instruction.synthetic.quoteArguments.QuoteArgument;
import instruction.synthetic.quoteArguments.VariableArgument;
import program.FunctionsHolder;
import program.Program;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionInstructionUtils {
    public static String buildCommandArguments(FunctionsHolder functionsHolder, List<QuoteArgument> arguments, Map<Variable, Variable> variableMapping) {

        return arguments.stream()
                .map(argument -> buildSingleArgument(functionsHolder, argument, variableMapping))
                .collect(Collectors.joining(","));
    }

    private static String buildSingleArgument(FunctionsHolder functionsHolder, QuoteArgument argument, Map<Variable, Variable> variableMapping) {

        return switch (argument) {
            case VariableArgument variableArgument -> {
                Variable innerVariable = variableArgument.getVariable();
                Variable mappedVariable = variableMapping.get(innerVariable);

                yield (mappedVariable != null)
                        ? mappedVariable.getRepresentation()
                        : innerVariable.getRepresentation();
            }

            case FunctionArgument functionArgument -> {
                String innerArgumentsAsString = functionArgument.getArguments().stream()
                        .map(innerArgument -> buildSingleArgument(functionsHolder, innerArgument, variableMapping))
                        .collect(Collectors.joining(","));

                String functionUserString = functionsHolder.getUserStringByName(functionArgument.getFunctionName());

                yield "(" + functionUserString
                        + (innerArgumentsAsString.isEmpty() ? "" : "," + innerArgumentsAsString)
                        + ")";
            }

            default -> throw new IllegalStateException(
                    "In FunctionInstructionUtils: Unsupported QuoteArgument type: " + argument.getClass()
            );
        };
    }

    // TODO: remove flag and dont map, delete the map as well
    public static List<QuoteArgument> mapFunctionArgumentsToNewList(List<QuoteArgument> arguments, Map<Variable, Variable> variableMapping, boolean isTopLevel) {
        List<QuoteArgument> mappedArguments = new ArrayList<>();

        for (QuoteArgument argument : arguments) {
            switch (argument) {
                case VariableArgument variableArgument -> {
                    Variable originalVariable = variableArgument.getVariable();
                    Variable newVariable;

                    if (isTopLevel) {
                        newVariable = variableMapping.get(originalVariable);
                        if (newVariable == null) {
                            throw new IllegalArgumentException(
                                    "In FunctionInstructionUtils: Variable not found in mapping: " + originalVariable.getRepresentation()
                            );
                        }
                    } else {
                        newVariable = originalVariable;
                    }


                    mappedArguments.add(new VariableArgument(newVariable));
                }

                case FunctionArgument functionArgument -> {
                    List<QuoteArgument> sameInnerArguments =    // Not mapped ! stayed the same
                            mapFunctionArgumentsToNewList(functionArgument.getArguments(), variableMapping, false);

                    mappedArguments.add(new FunctionArgument(functionArgument.getFunctionName(), sameInnerArguments));
                }

                default -> throw new IllegalStateException(
                        "In FunctionInstructionUtils: Unsupported QuoteArgument type: " + argument.getClass()
                );
            }
        }
        return mappedArguments;
    }
}
