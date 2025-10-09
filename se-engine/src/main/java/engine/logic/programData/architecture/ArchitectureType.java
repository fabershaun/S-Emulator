package engine.logic.programData.architecture;

import java.util.EnumSet;
import java.util.Set;

public enum ArchitectureType {

    A_0 (0, "A_0"), // Only for origin instruction

    A_1 (5, "I"),
    A_2 (100, "II"),
    A_3 (500, "III"),
    A_4 (1000, "IV")
    ;

    private final int creditsCost;
    private final String architectureRepresentation;

    ArchitectureType(int creditsCost, String architectureRepresentation) {
        this.creditsCost = creditsCost;
        this.architectureRepresentation = architectureRepresentation;
    }

    public int getCreditsCost() {
        return creditsCost;
    }

    public String getArchitectureRepresentation() {
        return architectureRepresentation;
    }

    // Create new ArchitectureType from representation
    public static ArchitectureType fromRepresentation(String representation) {
        for (ArchitectureType type : values()) {
            if (type.architectureRepresentation.equalsIgnoreCase(representation)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown architecture representation: " + representation);
    }

    // Return all architectures this one supports
    public Set<ArchitectureType> getSupportedArchitectures() {
        return EnumSet.range(A_0, this);
    }

    public boolean supports(ArchitectureType other) {
        return getSupportedArchitectures().contains(other);
    }

    public boolean isHigherThan(ArchitectureType other) {
        // Compare enum order
        return this.ordinal() > other.ordinal();
    }
}
