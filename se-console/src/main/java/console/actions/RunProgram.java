package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import engine.Engine;
import exceptions.EngineLoadException;

import java.util.List;
import java.util.Scanner;

public class RunProgram implements MenuActionable {
    Scanner scanner;
    Engine engine;

    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {

        System.out.println("Max degree of loaded program: " + engine.getMaxDegree());
        System.out.print("Please enter degree for this run: ");
        int degree = Validator.getValidateUserPath(scanner, engine);

        Long[] inputs = new Long[0];
        engine.displayUsedInputVariables();
        if (engine.getNumberOfInputVariables() != 0) {
            System.out.print("Please enter inputs values separated by commas: ");
            inputs = Validator.getValidateProgramInputs(scanner, engine);
        }

        engine.runProgram(degree, inputs);

        engine.displayProgramAfterRun();
    }
}
