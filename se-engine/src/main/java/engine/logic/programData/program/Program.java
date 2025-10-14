package engine.logic.programData.program;

import dto.v2.InstructionDTO;
import engine.logic.exceptions.EngineLoadException;
import engine.logic.programData.architecture.ArchitectureType;
import engine.logic.programData.instruction.Instruction;
import engine.logic.programData.label.Label;
import engine.logic.programData.variable.Variable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Program extends Serializable {

    ArchitectureType architectureRequired();
    void setMinimumArchitectureRequired(ArchitectureType minimumArchitectureRequired);
    ArchitectureType getMinimumArchitectureRequired();
    ProgramType getProgramType();

    String getName();
    String getUploaderName();
    String getUserString();

    Program getFunctionByName(String functionName);
    ProgramsHolder getProgramsHolder();

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
    Variable getResultVariable();

    void initialize();
    Program deepClone();
    void updateVariableAndLabel(Instruction instruction);
    void bucketVariableByFunctionInstruction(Set<Variable> variablesList);
    void addInstruction(Instruction instruction);
    void validateProgram() throws EngineLoadException;

    Label generateUniqueLabel();
    Variable generateUniqueVariable();
    void sortVariableSetByNumber(Set<Variable> variables);
    void addInputVariable(Variable variable);

    Map<Integer, Program> calculateDegreeToProgram();

    void addCreditCost(int creditCost);
    void incrementExecutionsCount();
    int getExecutionsCount();
    long getAverageCreditCost();

    String getMainProgramNameOfThisProgram();
}
