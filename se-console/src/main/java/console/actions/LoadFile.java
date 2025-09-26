package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import engine.Engine;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;


public class LoadFile implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {
        printTitle("Load New File");

        System.out.print("Please enter full path to your file: ");

        Path xmlPath = Validator.getValidateDegree(scanner);
        engine.loadProgram(xmlPath);
        engine.calculateExpansionForAllPrograms();  // Initialize expansion

        System.out.println("Successfully loaded the file");
    }
}
