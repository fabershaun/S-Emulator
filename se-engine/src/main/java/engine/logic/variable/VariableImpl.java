package engine.logic.variable;

import java.io.Serializable;

public record VariableImpl(VariableType type, int number) implements Variable, Serializable {
    public VariableImpl(VariableType type, int number) {
        this.type = type;
        this.number = (this.type == VariableType.RESULT) ? 0 : number;       // y will always get 0
    }

    @Override
    public String getRepresentation() {
        return type.getVariableRepresentation(number);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VariableImpl other)) return false;

        return this.number == other.number && this.type == other.type;
    }

}
