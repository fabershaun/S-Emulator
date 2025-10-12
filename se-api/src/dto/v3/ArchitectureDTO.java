package dto.v3;

import java.util.ArrayList;
import java.util.List;

public class ArchitectureDTO {
    private final List<String> architectureTypesStr;

    ArchitectureDTO(List<String> architectureTypesStr) {
        this.architectureTypesStr = architectureTypesStr;
    }

    public List<String> getArchitectureTypesStr() {
        return architectureTypesStr;
    }
}
