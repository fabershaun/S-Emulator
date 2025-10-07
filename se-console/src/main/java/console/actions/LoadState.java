package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import engine.Engine;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class LoadState implements MenuActionable {
    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {

        printTitle("Load State");

        System.out.print("Enter the full path from: ");
        Path file = Validator.getValidateExistingFile(scanner);

        //engine.loadState(file);

        System.out.println("State loaded successfully.");
    }
}
