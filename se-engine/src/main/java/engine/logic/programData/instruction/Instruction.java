package engine.logic.programData.instruction;

import dto.v2.InstructionDTO;
import dto.v3.UserDTO;
import engine.logic.execution.ExecutionContext;
import engine.logic.programData.architecture.ArchitectureType;
import engine.logic.programData.label.Label;
import engine.logic.programData.program.Program;
import engine.logic.programData.variable.Variable;

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
    long getConstantValue();
    int getInstructionNumber();
    Program getProgramOfThisInstruction();
    Program getMainProgram();
    String getCommand();
    List<Instruction> getExtendedInstruction();
    int getCycleOfInstruction();
    Instruction getOriginalInstruction();
    InstructionDTO getInstructionDTO();
    List<InstructionDTO> getInstructionExtendedList();

    Label execute(ExecutionContext context, UserDTO userDTO);
    Instruction createInstructionWithInstructionNumber(int instructionNumber);

    Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> variableMap, Map<Label, Label> labelMap, Instruction origin, Program mainProgram);
    void setMainProgram(Program mainProgram);

    ArchitectureType getArchitectureType();
}
