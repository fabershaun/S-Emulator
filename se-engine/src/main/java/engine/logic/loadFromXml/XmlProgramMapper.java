package engine.logic.loadFromXml;

import dto.v3.UserDTO;
import engine.logic.loadFromXml.generatedFromXml.*;
import engine.logic.programData.architecture.ArchitectureType;
import engine.logic.programData.instruction.synthetic.*;
import engine.logic.programData.instruction.AbstractInstruction;
import engine.logic.programData.instruction.Instruction;
import engine.logic.programData.instruction.OriginOfAllInstruction;
import engine.logic.programData.instruction.synthetic.quoteArguments.FunctionArgument;
import engine.logic.programData.instruction.synthetic.quoteArguments.QuoteArgument;
import engine.logic.programData.instruction.synthetic.quoteArguments.VariableArgument;
import engine.logic.programData.label.FixedLabel;
import engine.logic.programData.label.Label;
import engine.logic.programData.label.LabelImpl;
import engine.logic.programData.program.ProgramsHolder;
import engine.logic.programData.program.Program;
import engine.logic.programData.program.ProgramImpl;
import engine.logic.programData.variable.Variable;
import engine.logic.programData.variable.VariableImpl;
import engine.logic.programData.variable.VariableType;
import engine.logic.programData.instruction.basic.DecreaseInstruction;
import engine.logic.programData.instruction.basic.IncreaseInstruction;
import engine.logic.programData.instruction.basic.JumpNotZeroInstruction;
import engine.logic.programData.instruction.basic.NoOpInstruction;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class XmlProgramMapper {

    private XmlProgramMapper() {}

    static Program map(SProgram sProgram, ProgramsHolder programsHolder, UserDTO uploader, String uploaderName) {
        String programName = safeTrim(sProgram.getName());
        programName = programName != null ? programName : "Unnamed";

        validateUniqueMainProgramName(programsHolder, programName);

        Program targetProgram = new ProgramImpl(programName, programName, programsHolder, uploaderName);  // The user string of program is its name

        mapInstructionsIntoProgram(sProgram.getSInstructions(), targetProgram);

        Set<Program> innerFunctionsSet = new HashSet<>();

        // Map functions (sub-programs) if they exist
        if (sProgram.getSFunctions() != null) {
            for (SFunction sFunction : sProgram.getSFunctions().getSFunction()) {
                Program innerFunction = mapFunction(sFunction, programsHolder, uploaderName);
                validateUniqueFunctionName(programsHolder, innerFunction.getName());
                innerFunctionsSet.add(innerFunction);   // Temporary save the functions
            }
        }

        // After validate all the functions -> add them to the programHolder
        for (Program innerFunction : innerFunctionsSet) {
            programsHolder.addFunction(innerFunction.getName(), innerFunction.getUserString(), innerFunction);
            uploader.addOneToSubFunctionsCount();   // TODO: Need synchronized
        }

        return targetProgram;
    }

    private static void validateUniqueMainProgramName(ProgramsHolder programsHolder, String programName) {
        if (programsHolder.getMainProgramByName(programName) != null) {
            throw new IllegalArgumentException("Unable to upload file: main program with name " + programName + " already exists");
        }
    }

    private static void validateUniqueFunctionName(ProgramsHolder programsHolder, String programName) {
        if (programsHolder.getFunctionByName(programName) != null) {
            throw new IllegalArgumentException("Unable to upload file: function with name " + programName + " already exists");
        }
    }
    private static Program mapFunction(SFunction sFunction, ProgramsHolder programsHolder, String uploaderName) {
        String functionName = safeTrim(sFunction.getName());
        String userString = safeTrim(sFunction.getUserString());
        Program functionProgram = new ProgramImpl(
                functionName != null ? functionName : "UnnamedFunction",
                userString != null ? userString : "UnnamedUserString",
                programsHolder,
                uploaderName
        );

        // Map instructions of this function into its program
        mapInstructionsIntoProgram(sFunction.getSInstructions(), functionProgram);

        return functionProgram;
    }

    private static void mapInstructionsIntoProgram(SInstructions sInstructions, Program targetProgram) {
        if (sInstructions == null || sInstructions.getSInstruction() == null) {
            return; // No instructions to map
        }

        ArchitectureType maxArchitectureRequired = ArchitectureType.A_0; // the lowest
        ArchitectureType currentInstructionArchitecture;

        List<SInstruction> instructions = sInstructions.getSInstruction();
        for (int i = 0; i < instructions.size(); i++) {
            SInstruction sInstruction = instructions.get(i);
            Instruction mapped = mapSingleInstruction(targetProgram, sInstruction, i + 1);
            targetProgram.addInstruction(mapped);

            // To find the required architecture
            currentInstructionArchitecture = mapped.getArchitectureType();
            if (currentInstructionArchitecture.isHigherThan(maxArchitectureRequired)) {
                maxArchitectureRequired = currentInstructionArchitecture;
            }
        }

        targetProgram.setArchitectureRequired(maxArchitectureRequired);
    }

    private static Instruction mapSingleInstruction(Program targetProgram, SInstruction sInstruction, int ordinal) {
        try {
            String instructionName = toUpperSafe(sInstruction.getName());
            Label instructionLabel = parseLabel(sInstruction.getSLabel(), instructionName, ordinal);
            Variable targetVariable = parseVariable(sInstruction.getSVariable(), instructionName, ordinal);
            List<SInstructionArgument> sInstructionArguments = (sInstruction.getSInstructionArguments() != null) ?
                    sInstruction.getSInstructionArguments().getSInstructionArgument() :
                    null;
            Instruction originInstruction = new OriginOfAllInstruction(targetProgram, targetProgram);
            return createNewInstruction(targetProgram, instructionName, instructionLabel, targetVariable, sInstructionArguments, ordinal,
                    originInstruction);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static AbstractInstruction createNewInstruction(Program targetProgram,
                                                            String instructionName,
                                                            Label instructionLabel,
                                                            Variable targetVariable,
                                                            List<SInstructionArgument> sInstructionArguments,
                                                            int ordinal,
                                                            Instruction originInstruction) {
        switch (instructionName) {
            case "INCREASE":
                return new IncreaseInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, originInstruction, ordinal);

            case "DECREASE":
                return new DecreaseInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, originInstruction, ordinal);

            case "JUMP_NOT_ZERO":
            case "JNZ": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new JumpNotZeroInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, addedLabel, originInstruction, ordinal);
            }

            case "NO_OP":
            case "NEUTRAL":
                return new NoOpInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, originInstruction, ordinal);

            case "ZERO_VARIABLE":
                return new ZeroVariableInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, originInstruction, ordinal);

            case "GOTO_LABEL": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new GotoLabelInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, addedLabel, originInstruction, ordinal);
            }

            case "ASSIGNMENT": {
                String sourceVariableStr = sInstructionArguments.getFirst().getValue();
                Variable sourceVariable = parseVariable(sourceVariableStr, instructionName, ordinal);
                return new AssignmentInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, sourceVariable, originInstruction, ordinal);
            }

            case "CONSTANT_ASSIGNMENT": {
                int constantValue = Integer.parseInt(sInstructionArguments.getFirst().getValue());
                return new ConstantAssignmentInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, constantValue, originInstruction, ordinal);
            }

            case "JUMP_ZERO": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new JumpZeroInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, addedLabel, originInstruction, ordinal);
            }

            case "JUMP_EQUAL_CONSTANT": {
                Label addedLabel = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("JEConstantLabel"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .map(labelStr -> parseLabel(labelStr, instructionName, ordinal))
                        .orElseThrow(() -> new IllegalArgumentException("JEConstantLabel not found"));

                long constantValue = Long.parseLong(
                        sInstructionArguments.stream()
                                .filter(arg -> arg.getName().equalsIgnoreCase("constantValue"))
                                .map(SInstructionArgument::getValue)
                                .findFirst()
                                .get());

                return new JumpEqualConstantInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, constantValue, addedLabel, originInstruction, ordinal);
            }

            case "JUMP_EQUAL_VARIABLE": {
                Label addedLabel = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("JEVariableLabel"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .map(labelStr -> parseLabel(labelStr, instructionName, ordinal))
                        .orElseThrow(() -> new IllegalArgumentException("JEVariableLabel not found"));

                Variable sourceVariable = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("variableName"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .map(labelStr -> parseVariable(labelStr, instructionName, ordinal))
                        .orElseThrow(() -> new IllegalArgumentException("variableName not found"));

                return new JumpEqualVariableInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, sourceVariable, addedLabel, originInstruction, ordinal);
            }

            case "QUOTE": {
                String functionName = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionName"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .get()
                        .toUpperCase();

                String functionArguments = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionArguments"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .get();

                Set<Variable> innerVariablesInFunctionInstruction = extractXVariablesIntoSet(targetProgram.getInputVariables(), functionArguments);
                targetProgram.bucketVariableByFunctionInstruction(innerVariablesInFunctionInstruction); // To add inner input variable (that inside inner quote instruction) to the program
                List<QuoteArgument> quoteArguments = extractFunctionArguments(functionArguments);

                return new QuoteInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, originInstruction, ordinal, functionName, quoteArguments);
            }

            case "JUMP_EQUAL_FUNCTION": {
                Label addedLabel = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("JEFunctionLabel"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .map(labelStr -> parseLabel(labelStr, instructionName, ordinal))
                        .orElseThrow(() -> new IllegalArgumentException("JEFunctionLabel not found"));

                String functionArguments = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionArguments"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .get();

                String functionName = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionName"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .get()
                        .toUpperCase();

                Set<Variable> innerVariablesInFunctionInstruction = extractXVariablesIntoSet(targetProgram.getInputVariables(), functionArguments);
                targetProgram.bucketVariableByFunctionInstruction(innerVariablesInFunctionInstruction); // To add inner input variable (that inside inner quote instruction) to the program
                List<QuoteArgument> quoteArguments = extractFunctionArguments(functionArguments);

                return new JumpEqualFunctionInstruction(targetProgram, targetProgram, targetVariable, instructionLabel, addedLabel, originInstruction, ordinal, functionName, quoteArguments);
            }

            default:
                throw new IllegalArgumentException(
                        "Unknown instruction name at position " + ordinal + ": " + instructionName
                );
        }
    }

    private static List<QuoteArgument> extractFunctionArguments(String functionArguments) {
        if (functionArguments == null || functionArguments.isEmpty()) {
            return Collections.emptyList();
        }

        List<QuoteArgument> quoteArguments = new ArrayList<>();
        List<String> argumentsStrList = extractQuoteArgumentsToStrList(functionArguments);

        for (String argumentStr : argumentsStrList) {
            if (argumentStr.startsWith("(") && argumentStr.endsWith(")")) {
                int indexOfFirstComma = findFirstTopLevelComma(argumentStr);
                String functionName;
                String innerFunctionArgumentsStr;

                if (indexOfFirstComma != -1) {
                    functionName = argumentStr.substring(1, indexOfFirstComma);
                    innerFunctionArgumentsStr = argumentStr.substring(indexOfFirstComma + 1, argumentStr.length() - 1);
                } else {
                    // פונקציה בלי ארגומנטים
                    functionName = argumentStr.substring(1, argumentStr.length() - 1);
                    innerFunctionArgumentsStr = "";
                }

                List<QuoteArgument> innerArguments = extractFunctionArguments(innerFunctionArgumentsStr);
                quoteArguments.add(new FunctionArgument(functionName, innerArguments));
            } else {
                quoteArguments.add(new VariableArgument(argumentStr));
            }
        }

        return quoteArguments;
    }


    private static List<String> extractQuoteArgumentsToStrList(String functionArguments) {
        if (functionArguments == null || functionArguments.trim().isEmpty()) {
            return List.of();
        }

        String argumentsStr = functionArguments.trim();
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenthesesDepth = 0;

        for (char c : argumentsStr.toCharArray()) {
            if (c == ',' && parenthesesDepth == 0) {
                tokens.add(current.toString().trim());
                current.setLength(0);
            } else {
                if (c == '(') parenthesesDepth++;
                if (c == ')') parenthesesDepth--;
                current.append(c);
            }
        }

        if (!current.isEmpty()) {                   // To add the last part or to add the argument if there were no parentheses in the original string
            tokens.add(current.toString().trim());
        }

        return tokens;
    }

    private static int findFirstTopLevelComma(String str) {
        int depth = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == ',' && depth == 1) {
                return i;
            }
        }
        return -1;
    }

    private static Set<Variable> extractXVariablesIntoSet(Set<Variable> seenInputVariable, String functionArguments) {
        Pattern pattern = Pattern.compile("x(\\d+)");
        Matcher matcher = pattern.matcher(functionArguments);

        while (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            Variable newVariable = new VariableImpl(VariableType.INPUT, number);

            seenInputVariable.add(newVariable);
        }

        return seenInputVariable;
    }

    private static Label parseLabel(String raw, String where, int ordinal) {
        String trimmed = safeTrim(raw);
        if (trimmed == null || trimmed.isEmpty()) {
            return FixedLabel.EMPTY;
        }

        Label label;
        if (trimmed.equalsIgnoreCase(FixedLabel.EXIT.getLabelRepresentation())) {
            label = FixedLabel.EXIT;
        }

        else if (trimmed.matches("L\\d+")) {
            String numberPart = trimmed.substring(1);
            int labelNumber = Integer.parseInt(numberPart);
            label = new LabelImpl(labelNumber);
        }
        else {
            throw new IllegalArgumentException(
                    "Problem creating the label: " + raw + System.lineSeparator() +
                            "Instruction number: " + ordinal + System.lineSeparator() +
                                "Instruction name: " + where
            );
        }

        return label;
    }

    private static Variable parseVariable(String token, String where, int ordinal) {
        String trimmed = safeTrim(token);
        if (trimmed == null || trimmed.isEmpty()) return null;

        if (trimmed.equalsIgnoreCase("y")) {
            return new VariableImpl(VariableType.RESULT, 1);
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("^([xz])(\\d+)$", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(trimmed);
        if (matcher.matches()) {
            String prefix = matcher.group(1);
            int number = Integer.parseInt(matcher.group(2));

            VariableType variableType;
            if(prefix.equalsIgnoreCase("x")) variableType = VariableType.INPUT;
            else if(prefix.equalsIgnoreCase("z")) variableType = VariableType.WORK;
            else throw new IllegalArgumentException(
                    "Unsupported variable type: " + prefix + "for " + where + " at instruction number " + ordinal);

            return new VariableImpl(variableType, number);
        }

        throw new IllegalArgumentException(
                "Problem creating the variable: " + trimmed + ". expected x / y / z" + System.lineSeparator() +
                        "The problem accour at instruction number: " + ordinal + " named: " + where
        );
    }

    private static String safeTrim(String s) { return s == null ? null : s.trim().toUpperCase(Locale.ROOT); }

    private static String toUpperSafe(String s) { return s == null ? null : s.trim().toUpperCase(Locale.ROOT); }
}
