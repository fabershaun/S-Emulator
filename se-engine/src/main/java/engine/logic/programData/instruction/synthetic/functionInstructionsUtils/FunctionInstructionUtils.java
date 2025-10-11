package engine.logic.programData.instruction.synthetic.functionInstructionsUtils;

import dto.v3.UserDTO;
import engine.logic.execution.ExecutionContext;
import engine.logic.execution.runMode.ProgramExecutor;
import engine.logic.execution.runMode.ProgramExecutorImpl;
import engine.logic.programData.instruction.synthetic.quoteArguments.FunctionArgument;
import engine.logic.programData.instruction.synthetic.quoteArguments.QuoteArgument;
import engine.logic.programData.instruction.synthetic.quoteArguments.VariableArgument;
import engine.logic.programData.program.ProgramsHolder;
import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static engine.logic.programData.instruction.synthetic.functionInstructionsUtils.FunctionExecutionResult.extractInputValues;

public class FunctionInstructionUtils {

    public static List<FunctionExecutionResult> getInputs(
            List<QuoteArgument> innerQuoteArgumentsList,
            ExecutionContext context,
            Program mainProgram,
            UserDTO userDTO) {

        List<FunctionExecutionResult> inputs = new ArrayList<>();

        for (QuoteArgument quoteFunctionArgument : innerQuoteArgumentsList) {
            switch (quoteFunctionArgument.getType()) {
                case FUNCTION -> {
                    FunctionArgument functionArgument = (FunctionArgument) quoteFunctionArgument;
                    FunctionExecutionResult functionExecutionResult = calculateFunctionResult(functionArgument, context, mainProgram, userDTO);
                    inputs.add(functionExecutionResult);
                }
                case VARIABLE -> {
                    VariableArgument innerVariableArgument = (VariableArgument) quoteFunctionArgument;
                    long inputValue = innerVariableArgument.getInputValueFromContext(context);
                    inputs.add(new FunctionExecutionResult(inputValue, 0)); // cycles 0
                }
            }
        }

        return inputs;
    }

    // Recursive function: the goal is to reach functions that hold only variable arguments
    public static FunctionExecutionResult calculateFunctionResult(
            FunctionArgument innerFunctionArgument,
            ExecutionContext context,
            Program mainProgram,
            UserDTO userDTO) {

        String innerFunctionName = innerFunctionArgument.getFunctionName();
        Program innerFunction = mainProgram.getFunctionByName(innerFunctionName);

        ProgramExecutor functionExecutor = new ProgramExecutorImpl(innerFunction, null);
        List<FunctionExecutionResult> functionExecutionResultList = getInputs(innerFunctionArgument.getArguments(), context, mainProgram, userDTO);

        // Run
        functionExecutor.run(userDTO, 0, extractInputValues(functionExecutionResultList));    // architectureTypeSelected - not needed here (it's an inner call)

        // Return function result
        Variable resultVariable = innerFunction.getResultVariable();
        long resultValue = functionExecutor.getVariableValue(resultVariable);
        int cycles = functionExecutor.getTotalCycles();
        return new FunctionExecutionResult(resultValue, cycles);
    }

    public static String buildCommandArguments(ProgramsHolder programsHolder, List<QuoteArgument> arguments, Map<Variable, Variable> variableMapping) {
        return arguments.stream()
                .map(argument -> buildSingleArgument(programsHolder, argument, variableMapping))
                .collect(Collectors.joining(","));
    }

    private static String buildSingleArgument(ProgramsHolder programsHolder, QuoteArgument argument, Map<Variable, Variable> variableMapping) {

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
                        .map(innerArgument -> buildSingleArgument(programsHolder, innerArgument, variableMapping))
                        .collect(Collectors.joining(","));

                String functionUserString = programsHolder.getNameByUserString(functionArgument.getFunctionName());

                yield "(" + functionUserString
                        + (innerArgumentsAsString.isEmpty() ? "" : "," + innerArgumentsAsString)
                        + ")";
            }

            default -> throw new IllegalStateException(
                    "In FunctionInstructionUtils: Unsupported QuoteArgument type: " + argument.getClass()
            );
        };
    }

    public static List<QuoteArgument> mapFunctionArgumentsToNewList(List<QuoteArgument> arguments, Map<Variable, Variable> variableMapping, boolean toMapFlag) {
        List<QuoteArgument> mappedArguments = new ArrayList<>();

        for (QuoteArgument argument : arguments) {
            switch (argument) {
                case VariableArgument variableArgument -> {
                    Variable originalVariable = variableArgument.getVariable();
                    Variable newVariable;

                    if (toMapFlag) {
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
                            mapFunctionArgumentsToNewList(functionArgument.getArguments(), variableMapping, toMapFlag);

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
