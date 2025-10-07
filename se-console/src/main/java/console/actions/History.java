package console.actions;

import console.menu.MenuActionable;
import dto.ProgramExecutorDTO;
import engine.Engine;

import java.util.List;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class History implements MenuActionable {
    @Override
    public void startAction(Scanner scanner, Engine engine) {
        printTitle("Present History");

        String programName = engine.getMainProgramToConsoleModule().getProgramName(); // Added because gui module
        List<ProgramExecutorDTO> programExecutorDTOList = engine.getHistoryPerProgram(programName);

        if (!programExecutorDTOList.isEmpty()) {
            displayHistory(programExecutorDTOList);
        } else {
            System.out.println("No history has been set in this program. Run the program first.");
        }
    }

    private void displayHistory(List<ProgramExecutorDTO> programExecutorDTOList) {
        int i = 1;

        for (ProgramExecutorDTO programExecutorDTO : programExecutorDTOList) {
            System.out.println("#" + i);
            System.out.println("Run degree: " + programExecutorDTO.getDegree());

            var inputs = programExecutorDTO.getInputsValuesOfUser();
            String inputsDisplay = (inputs == null || inputs.isEmpty())
                    ? "no input from user"
                    : inputs.stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(", "));

            System.out.println("Inputs values: " + inputsDisplay);
            System.out.println("Result: " + programExecutorDTO.getResult());
            System.out.println("Cycles: " + programExecutorDTO.getTotalCycles());
            System.out.println();

            i++;
        }
    }
}
