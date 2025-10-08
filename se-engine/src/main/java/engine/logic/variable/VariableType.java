package engine.logic.variable;

public enum VariableType {
    INPUT ("x", 0),
    WORK  ("z", 1),
    RESULT("y", 2);

    private final String prefix;
    private final int rank;

    VariableType(String prefix, int rank) {
        this.prefix = prefix;
        this.rank = rank;
    }

    public String getVariableRepresentation(int number) {
        return this == RESULT ? prefix : prefix + number;           // y will get only the letter
    }

    public int rank() { return rank; }
    public String prefix() { return prefix; }
}

/*
public enum VariableType {
    INPUT{
        public String getVariableRepresentation(int number) {
            return "x" + number;
        }
        public int rank() { return 0; }
    },
    WORK{
        public String getVariableRepresentation(int number) {
            return "z" + number;
        }
        public int rank() { return 1; }
    },
    RESULT{
        public String getVariableRepresentation(int number) {
            return "y";
        }
        public int rank() { return 2; }
    };

    public abstract String getVariableRepresentation(int number);
    public abstract int rank();
}
*/
