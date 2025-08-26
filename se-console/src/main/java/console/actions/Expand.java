package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import engine.Engine;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.Scanner;

public class Expand implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {

        System.out.println("Max degree of loaded program: " + engine.getMaxDegree());

        if (engine.getMaxDegree() == 0) {
            throw new IllegalStateException("The program cannot be increased because its maximum level is 0.");
        }

        System.out.print("Please enter degree for this run: ");
        int degree = Validator.getValidateUserPath(scanner, engine);

        engine.displayExpandedProgram(degree);
    }


}