package engine.logic.exceptions;

public class CreditsException extends RuntimeException {
    public CreditsException(String message) {
        super(message);
    }

    public CreditsException(long currentCredits, long requiredCredits) {
        super("Not enough credits. Current: " + currentCredits + ", required: " + requiredCredits);
    }
}

