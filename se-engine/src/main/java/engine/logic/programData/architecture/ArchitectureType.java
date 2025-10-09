package engine.logic.programData.architecture;

public enum ArchitectureType {
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
}
