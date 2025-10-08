package engine.logic.exceptions;

public class EngineLoadException extends Exception {
    public EngineLoadException(String message) { super(message); }
    public EngineLoadException(String message, Throwable cause) { super(message, cause); }
}
