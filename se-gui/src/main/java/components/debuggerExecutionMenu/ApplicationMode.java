package components.debuggerExecutionMenu;

public enum ApplicationMode {
    NO_PROGRAM_LOADED,
    PROGRAM_READY,       // After 'New Run' (before the user choose run/ debug)
    NEW_RUN_PRESSED,
    RUN,
    DEBUG
    }
