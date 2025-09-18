package instruction;

import dto.InstructionDTO;
import execution.ExecutionContext;
import label.Label;
import program.Program;
import variable.Variable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Instruction extends Serializable {

    String getName();
    String getInstructionType();
    Label getLabel();
    Label getReferenceLabel();
    Variable getTargetVariable();
    Variable getSourceVariable();
    int getInstructionNumber();
    String getCommand();
    List<Instruction> getExtendedInstruction();
    int getCycleOfInstruction();
    Instruction getOriginalInstruction();
    InstructionDTO getInstructionDTO();
    List<InstructionDTO> getInstructionExtendedList();

//    void setProgramOfThisInstruction(Program programOfThisInstruction);
    Label execute(ExecutionContext context);
    Instruction createInstructionWithInstructionNumber(int instructionNumber);

    Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap);
}
