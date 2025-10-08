package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import dto.InstructionDTO;
import dto.ProgramDTO;
import engine.Engine;
import engine.logic.exceptions.EngineLoadException;

import java.util.List;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class Expand implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {
        printTitle("Present Expand Program");
        String programName = engine.getMainProgramToConsoleModule().getProgramName(); // Added because gui module

        if (engine.getMaxDegree(programName) == 0) {
            System.out.println("The program cannot be expand because its maximum degree is already 0");
            return;
        }

        System.out.println("Max degree of loaded program: " + engine.getMaxDegree(programName));

        System.out.print("Please enter degree for this run: ");
        int degree = Validator.getValidateDegree(scanner, engine, programName);

        ProgramDTO programDTO = engine.getExpandedProgramDTO(programName, degree);

        displayExpandedProgram(programDTO);
    }

    private void displayExpandedProgram(ProgramDTO programDTO) {
        List<List<InstructionDTO>> lines = programDTO.getExpandedProgram();
        if (lines == null || lines.isEmpty()) {
            System.out.println("(no instructions)");
            return;
        }

        int total = lines.size();

        for (List<InstructionDTO> line : lines) {
            String joined = line.stream()
                    .map(dto -> Display.getInstructionRepresentation(dto, total, true))
                    .collect(java.util.stream.Collectors.joining(" >>> "));
            System.out.println(joined);
        }
    }
}