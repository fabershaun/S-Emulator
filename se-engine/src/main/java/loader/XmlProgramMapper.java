package loader;

import generatedFromXml.*;
import instruction.AbstractInstruction;
import instruction.Instruction;
import instruction.OriginOfAllInstruction;
import label.FixedLabel;
import label.Label;
import label.LabelImpl;
import program.AbstractProgram;
import program.FunctionImpl;
import program.Program;
import program.ProgramImpl;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;
import instruction.basic.DecreaseInstruction;
import instruction.basic.IncreaseInstruction;
import instruction.basic.JumpNotZeroInstruction;
import instruction.basic.NoOpInstruction;
import instruction.synthetic.*;

import java.util.List;
import java.util.Locale;

final class XmlProgramMapper {

    private XmlProgramMapper() {}

    static Program map(SProgram sProgram) {
        String programName = safeTrim(sProgram.getName());
        ProgramImpl targetProgram = new ProgramImpl(programName != null ? programName : "Unnamed");

        mapInstructionsIntoProgram(sProgram.getSInstructions(), targetProgram);

        // Map functions (sub-programs) if they exist
        if (sProgram.getSFunctions() != null) {
            for (SFunction sFunction : sProgram.getSFunctions().getSFunction()) {
                FunctionImpl innerFunction = mapFunction(sFunction);
                targetProgram.addInnerFunction(innerFunction.getName(), innerFunction);
            }
        }

        return targetProgram;
    }

    private static FunctionImpl mapFunction(SFunction sFunction) {
        String functionName = safeTrim(sFunction.getName());
        String userString = safeTrim(sFunction.getUserString());
        FunctionImpl functionProgram = new FunctionImpl(
                functionName != null ? functionName : "UnnamedFunction",
                userString != null ? userString : "UnnamedUserString"
        );

        // Map instructions of this function into its program
        mapInstructionsIntoProgram(sFunction.getSInstructions(), functionProgram);

        return functionProgram;
    }

    private static void mapInstructionsIntoProgram(SInstructions sInstructions, Program targetProgram) {
        if (sInstructions == null || sInstructions.getSInstruction() == null) {
            return; // No instructions to map
        }

        List<SInstruction> instructions = sInstructions.getSInstruction();
        for (int i = 0; i < instructions.size(); i++) {
            SInstruction sInstruction = instructions.get(i);
            Instruction mapped = mapSingleInstruction(sInstruction, i + 1);
            mapped.setProgramOfThisInstruction(targetProgram);
            targetProgram.addInstruction(mapped);
        }
    }

    private static Instruction mapSingleInstruction(SInstruction sInstruction, int ordinal) {
        try {
            String instructionName = toUpperSafe(sInstruction.getName());
            Label instructionLabel = parseLabel(sInstruction.getSLabel(), instructionName, ordinal);
            Variable targetVariable = parseVariable(sInstruction.getSVariable(), instructionName, ordinal);
            List<SInstructionArgument> sInstructionArguments = (sInstruction.getSInstructionArguments() != null) ?
                    sInstruction.getSInstructionArguments().getSInstructionArgument() :
                    null;
            Instruction originInstruction = new OriginOfAllInstruction();
            return createNewInstruction(instructionName, instructionLabel, targetVariable, sInstructionArguments, ordinal, originInstruction);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static AbstractInstruction createNewInstruction(String instructionName,
                                                            Label instructionLabel,
                                                            Variable targetVariable,
                                                            List<SInstructionArgument> sInstructionArguments,
                                                            int ordinal,
                                                            Instruction originInstruction) {
        switch (instructionName) {
            case "INCREASE":
                return new IncreaseInstruction(targetVariable, instructionLabel, originInstruction, ordinal);

            case "DECREASE":
                return new DecreaseInstruction(targetVariable, instructionLabel, originInstruction, ordinal);

            case "JUMP_NOT_ZERO": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new JumpNotZeroInstruction(targetVariable, instructionLabel, addedLabel, originInstruction, ordinal);
            }

            case "NEUTRAL":
                return new NoOpInstruction(targetVariable, instructionLabel, originInstruction, ordinal);

            case "ZERO_VARIABLE":
                return new ZeroVariableInstruction(targetVariable, instructionLabel, originInstruction, ordinal);

            case "GOTO_LABEL": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new GotoLabelInstruction(targetVariable, instructionLabel, addedLabel, originInstruction, ordinal);
            }

            case "ASSIGNMENT": {
                String sourceVariableStr = sInstructionArguments.getFirst().getValue();
                Variable sourceVariable = parseVariable(sourceVariableStr, instructionName, ordinal);
                return new AssignmentInstruction(targetVariable, instructionLabel, sourceVariable, originInstruction, ordinal);
            }

            case "CONSTANT_ASSIGNMENT": {
                int constantValue = Integer.parseInt(sInstructionArguments.getFirst().getValue());
                return new ConstantAssignmentInstruction(targetVariable, instructionLabel, constantValue, originInstruction, ordinal);
            }

            case "JUMP_ZERO": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new JumpZeroInstruction(targetVariable, instructionLabel, addedLabel, originInstruction, ordinal);
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

                return new JumpEqualConstantInstruction(targetVariable, instructionLabel, constantValue, addedLabel, originInstruction, ordinal);
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

                return new JumpEqualVariableInstruction(targetVariable, instructionLabel, sourceVariable, addedLabel, originInstruction, ordinal);
            }

            case "QUOTE": {
                String functionName = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionName"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .get();

                String functionArguments = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionArguments"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .get();

                return new QuoteInstruction(targetVariable, instructionLabel, originInstruction, ordinal, functionName, functionArguments);
            }

            default:
                throw new IllegalArgumentException(
                        "Unknown instruction name at position " + ordinal + ": " + instructionName
                );
        }
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
