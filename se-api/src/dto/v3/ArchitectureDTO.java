package dto.v3;

public class ArchitectureDTO {
    private final String representation;
    private final int rank;

    public ArchitectureDTO(String representation, int rank) {
        this.representation = representation;
        this.rank = rank;
    }

    public String getRepresentation() {
        return representation;
    }

    public int getRank() {
        return rank;
    }
}
