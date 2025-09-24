package program;

import dto.InstructionDTO;
import exceptions.EngineLoadException;
import instruction.Instruction;
import label.Label;
import variable.Variable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Program extends Serializable {

    String getName();
    String getUserString();
    FunctionsHolder getFunctionsHolder();
    List<Instruction> getInstructionsList();
    Instruction getInstructionByLabel(Label label);
    Set<Variable> getInputVariables();
    Set<Variable> getWorkVariables();
    List<Variable> getInputAndWorkVariablesSortedBySerial();
    List<Label> getLabelsInProgram();
    Set<Label> getReferenceLabelsInProgram();
    Map<Label, Instruction> getLabelToInstruction();  // Instructions start counting from 1
    List<String> getOrderedLabelsExitLastStr();
    List<String> getInputVariablesSortedStr();
    List<String> getWorkVariablesSortedStr();
    List<List<InstructionDTO>> getExpandedProgram();
    List<InstructionDTO> getInstructionDtoList();

    void initialize();
    Program deepClone();
    void addInstruction(Instruction instruction);
    void validateProgram() throws EngineLoadException;
    int calculateProgramMaxDegree();
    void expandProgram(int degree);
    Label generateUniqueLabel();
    Variable generateUniqueVariable();
    void sortVariableSetByNumber(Set<Variable> variables);
    void addInputVariable(Variable variable);

    Variable findVariableByName(String name);
    Variable getResultVariable();
}
