package console;

/*
 .\xjc-run-win.bat -p engine.generated -d "..\..\se-engine\src\main\java" S-Emulator-v1.xsd
 */

public class Main {

    public static void main(String[] args)  {
        UIManager uiManager = new UIManager();
        uiManager.run();
    }
}
