package console.menu;

import engine.Engine;
import exceptions.EngineLoadException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MenuItem implements Menu {

    public String title;
    public String commandLine;
    private final List<MenuItem> subItems = new ArrayList<>();
    private MenuActionable actionToExecute;
    private Engine engine;
    private String mainProgramName;
    Scanner scanner;


    public MenuItem(String title) {
        this.title = title;
    }

    public MenuItem(String commandLine, MenuActionable action, Engine engine) {
        this.commandLine = commandLine;
        this.actionToExecute = action;
        this.engine = engine;
    }

    @Override
    public boolean isLeaf() {
        return subItems.isEmpty();
    }

    @Override
    public void addSubItem(MenuItem newSubItem) {
        this.subItems.add(newSubItem);
    }

    @Override
    public boolean show(Scanner scanner, Engine engine) {
        boolean exitAll = false;
        this.scanner = scanner;
        this.engine = engine;

        while (!exitAll) {
            printCurrentMenu();

            try {
                int userChoice =  getValidateUserChoice(scanner);
                exitAll = handleChoice(userChoice);
            }
            catch (IllegalArgumentException  e) {
                System.out.println(e.getMessage());
            }
        }
        return true;
    }

    private int getValidateUserChoice(Scanner scanner) {
        String input = scanner.nextLine().trim();
        int max = subItems.size() + 1;

        if (input.isEmpty()) {
            throw new IllegalArgumentException("Invalid input. Choice cannot be empty.");
        }

        final int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid input, numbers only between 1 and " + max + ". Got: " + input
            );
        }

        if (choice < 1 || choice > max) {
            throw new IllegalArgumentException(
                    "Choice out of range, enter a number between 1 and " + max + ". Got: " + choice
            );
        }

        return choice;
    }

    private boolean handleChoice(int userChoice) {
        int exitNumber = this.subItems.size() + 1;

        if (userChoice == exitNumber) {
            System.out.println("Good Bye!");
            return true;    // To get out from all the menus
        }
        else {
            MenuItem selectedItem = subItems.get(userChoice - 1);

            try {
                execute(selectedItem);

                if (!selectedItem.isLeaf()) {
                    boolean exitAll = selectedItem.show(scanner, engine);
                    if (exitAll) {
                        return true;    // To get out from all the menus
                    }
                }
            } catch (EngineLoadException e) {
                System.out.println(e.getMessage());
            }
        }

        return false;   // To stay at the current menu
    }

    @Override
    public void printCurrentMenu() {
        int numberOfSubItems = this.subItems.size();
        printTitle("Menu");

        for (int i = 0; i < numberOfSubItems; i++) {
            System.out.printf("%d. %s%n", i + 1, subItems.get(i).commandLine);
        }

        System.out.printf("%d. %s%n", numberOfSubItems + 1, "Exit");
        System.out.printf("Please enter your choice (1-%d): ", subItems.size() + 1);
    }

    public static void printTitle(String title) {
        if (title == null) title = "";
        int totalWidth = 60;
        int inner = Math.max(0, totalWidth - title.length() - 2);
        int left = inner / 2;
        int right = inner - left;
        System.out.printf("%s %s %s%n", "=".repeat(left), title, "=".repeat(right));
    }

    private void execute(MenuItem selectedItem) throws EngineLoadException {
        mainProgramName = selectedItem.actionToExecute.startAction(scanner, engine);
        System.out.println();
    }
}
