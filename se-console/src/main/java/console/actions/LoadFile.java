package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import dto.v3.UserDTO;
import engine.Engine;
import engine.logic.exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class LoadFile implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {
        printTitle("Load New File");

        System.out.print("Please enter full path to your file: ");

        Path xmlPath = Validator.getValidateDegree(scanner);
        engine.loadProgramFromFile(xmlPath, UserDTO.DEFAULT_NAME);

        System.out.println("Successfully loaded the file");
    }
}
