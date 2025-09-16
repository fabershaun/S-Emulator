package program;

public class FunctionImpl extends AbstractProgram {
    private final String userString;

    public FunctionImpl(String name, String userString) {
        super(name);
        this.userString = userString;
    }

    public String getUserString() {
        return userString;
    }
}
