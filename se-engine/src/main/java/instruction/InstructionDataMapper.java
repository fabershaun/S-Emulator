package instruction;

import dto.InstructionDataDTO;

import java.util.ArrayList;
import java.util.List;

public class InstructionDataMapper {

    public static int getCyclesOfInstruction(String kind) {
        for (InstructionData data : InstructionData.values()) {
            if (data.getName().equals(kind)) {
                return data.getCycles();
            }
        }

        throw new IllegalArgumentException("Invalid kind: " + kind);
    }

    public static List<InstructionDataDTO> getAvailableInstructions() {
        List<InstructionDataDTO> list = new ArrayList<>();
        for (InstructionData data : InstructionData.values()) {
            if (data == InstructionData.QUOTATION || data == InstructionData.JUMP_EQUAL_FUNCTION || data == InstructionData.ORIGIN) {
                continue; // Skip unwanted
            }
            list.add(new InstructionDataDTO(data.getName(), data.getCycles()));
        }
        return list;
    }
}
