package engine.logic.programData.architecture;

import java.util.EnumSet;
import java.util.Set;

public enum ArchitectureType {

    A_0 (0), // Only for origin instruction

    A_1 (5),
    A_2 (100),
    A_3 (500),
    A_4 (1000)
    ;

    private final int creditsCost;

    ArchitectureType(int creditsCost) {
        this.creditsCost = creditsCost;
    }

    public int getCreditsCost() {
        return creditsCost;
    }

    // Return all architectures this one supports
    public Set<ArchitectureType> getSupportedArchitectures() {
        return EnumSet.range(A_0, this);
    }

    public boolean supports(ArchitectureType other) {
        return getSupportedArchitectures().contains(other);
    }
}
