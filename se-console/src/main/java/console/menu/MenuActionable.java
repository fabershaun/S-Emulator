package console.menu;

import engine.Engine;
import engine.logic.exceptions.EngineLoadException;

import java.util.Scanner;

public interface MenuActionable {

    void startAction(Scanner scanner, Engine engine) throws EngineLoadException;

}
