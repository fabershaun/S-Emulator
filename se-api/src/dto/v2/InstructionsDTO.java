package dto.v2;

import java.util.List;

public class InstructionsDTO {
    private final List<InstructionDTO> programInstructionsDtoList;

    public InstructionsDTO(List<InstructionDTO> programInstructionsDtoList) {
        this.programInstructionsDtoList = programInstructionsDtoList;
    }

    public List<InstructionDTO> getProgramInstructionsDtoList() {
        return programInstructionsDtoList;
    }
}
