package console.menu;

import engine.Engine;
import exceptions.EngineLoadException;

import java.util.Scanner;

public interface MenuActionable {

    String startAction(Scanner scanner, Engine engine) throws EngineLoadException;

}
