package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import exceptions.EngineLoadException;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class RunProgram implements MenuActionable {
    private Engine engine;
    private Scanner scanner;

    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {
        this.engine = engine;
        this.scanner = scanner;

        printTitle("Run Loaded Program");

        int degree = getMaxDegree();

        displayInputVariables();
        Long[] inputs = getInputs();

        engine.runProgram(degree, inputs);

        ProgramExecutorDTO programExecutorDTO = engine.getProgramAfterRun();
        displayProgramAfterRun(programExecutorDTO);
    }

    private void displayInputVariables() {
        List<String> variablesInputInProgram = engine.getProgram().getInputVariables();

        String displayInputVariables = String.format(
                "Inputs: %s", String.join(", ", variablesInputInProgram)
        );

        System.out.println(displayInputVariables);
    }

    private Long[] getInputs() {
        Long[] inputs = new Long[0];

        if (engine.getNumberOfInputVariables() != 0) {
            System.out.print("Please enter inputs values separated by commas: ");
            inputs = Validator.getValidateProgramInputValues(scanner);
        }

        return inputs;
    }

    private int getMaxDegree() throws EngineLoadException {
        int degree = 0;

        if (engine.getMaxDegree() == 0 ) {
            System.out.println("The program must run on degree zero because its maximum degree is already 0");
        } else {
            System.out.println("Max degree of loaded program: " + engine.getMaxDegree());
            System.out.print("Please enter degree for this run: ");
            degree = Validator.getValidateDegree(scanner, engine);
        }

        return degree;
    }

    private void displayProgramAfterRun(ProgramExecutorDTO programExecutorDTO) {
        ProgramDTO programDTO = programExecutorDTO.getProgramDTO();

        Display.displayProgram(programDTO);
        System.out.println();

        System.out.printf("Result: %d%n%n", programExecutorDTO.getResult());

        displayVariablesSorted(programExecutorDTO);

        System.out.printf("Cycles: %d%n%n", programExecutorDTO.getTotalCycles());
    }

    private void displayVariablesSorted(ProgramExecutorDTO programExecutorDTO) {
        System.out.println("Variables:");
        System.out.printf("y = %d%n", programExecutorDTO.getResult());

        for (Map.Entry<String, Long> entry : programExecutorDTO.getVariablesToValuesSorted().entrySet()) {
            System.out.printf("%s = %d%n", entry.getKey(), entry.getValue());
        }
        System.out.println();
    }
}
