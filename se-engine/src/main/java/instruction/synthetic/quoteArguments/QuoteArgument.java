package instruction.synthetic.quoteArguments;

import java.io.Serializable;

public abstract class QuoteArgument implements Serializable {
    private long value = 0;  // Default value

    public abstract ArgumentType getType();

    public abstract String getArgumentStr();

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public enum ArgumentType {
        VARIABLE,
        FUNCTION
    }
}