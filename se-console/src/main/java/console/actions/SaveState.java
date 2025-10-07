package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import engine.Engine;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class SaveState implements MenuActionable {
    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {

        printTitle("Save Current State");

        System.out.print("Enter target directory: ");
        Path directory = Validator.getValidateExistingDirectory(scanner);

        System.out.print("Enter file name: ");
        String fileName = Validator.getValidateNewFileName(scanner);

        Path fullPath = directory.resolve(fileName).toAbsolutePath().normalize();
        //engine.saveState(fullPath);

        System.out.println("State saved successfully.");
    }
}
