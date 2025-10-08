package console.actions;

import console.menu.MenuActionable;
import dto.v2.InstructionDTO;
import dto.v2.InstructionsDTO;
import dto.v2.ProgramDTO;
import engine.Engine;

import java.util.List;
import java.util.Scanner;
import java.util.stream.LongStream;

import static console.menu.MenuItem.printTitle;

public class Display implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) {

        printTitle("Display Program");
        ProgramDTO programDTO = engine.getMainProgramToConsoleModule();
        displayProgram(programDTO);
    }

    public static void displayProgram(ProgramDTO programDTO) {
        List<String> variablesInputInProgram = programDTO.getInputVariables();
        List<String> labels = java.util.Optional.ofNullable(programDTO.getLabelsStr()).orElse(java.util.List.of());

        String labelsDisplay = labels.isEmpty()
                ? "no labels in program"
                : String.join(", ", labels);

        String display = String.format(
                "Name: %s%n" +
                "Inputs: %s%n" +
                "Labels: %s%n" +
                "Instructions:%n%s",
                programDTO.getProgramName(),
                String.join(", ", variablesInputInProgram),
                labelsDisplay,
                getProgramInstructionsRepresentation(programDTO.getInstructions())
        );

        System.out.println(display);
    }

    public static String getProgramInstructionsRepresentation(InstructionsDTO instructionsDTO) {
        StringBuilder instructionsRepresentation = new StringBuilder();
        int n = instructionsDTO.getProgramInstructionsDtoList().size();

        for (InstructionDTO instructionDTO : instructionsDTO.getProgramInstructionsDtoList()) {
            instructionsRepresentation.append(getInstructionRepresentation(instructionDTO, n, false)).append(System.lineSeparator());
        }

        return instructionsRepresentation.toString();
    }

    public static String getInstructionRepresentation(InstructionDTO instructionDTO, int numberOfInstructionsInProgram, boolean expandView) {
        int labelPadding = 3;
        int numberPadding = numberOfInstructionsInProgram == 0 || expandView
                ? 1
                : (int) LongStream.iterate(Math.abs(numberOfInstructionsInProgram), x -> x > 0, x -> x / 10).count();

        return String.format(
                "#%" + numberPadding + "d (%s)[ %-" + labelPadding + "s ] %-" + 5 + "s (%d)",
                instructionDTO.getInstructionNumber(),
                instructionDTO.getInstructionTypeStr(),
                instructionDTO.getLabelStr(),
                instructionDTO.getCommand(),
                instructionDTO.getCyclesNumber()
        );
    }
}
