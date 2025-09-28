package instruction;

import dto.InstructionDataDTO;

import java.util.ArrayList;
import java.util.List;

public class InstructionDataMapper {
    public static List<InstructionDataDTO> getAvailableInstructions() {
        List<InstructionDataDTO> list = new ArrayList<>();
        for (InstructionData data : InstructionData.values()) {
            if (data == InstructionData.QUOTATION || data == InstructionData.JUMP_EQUAL_FUNCTION) {
                continue; // Skip unwanted
            }
            list.add(new InstructionDataDTO(data.getName(), data.getCycles()));
        }
        return list;
    }
}
