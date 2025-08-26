package program;

import exceptions.EngineLoadException;
import instruction.Instruction;
import label.Label;
import variable.Variable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Program {

    void setNextLabelNumber(int nextLabelNumber);
    void setNextWorkVariableNumber(int nextLabelNumber);

    int getNextLabelNumber();
    int getNextWorkVariableNumber();
    String getName();
    List<Instruction> getInstructionsList();
    Instruction getInstructionByLabel(Label label);
    Set<Variable> getInputVariables();
    List<String> getInputVariableSorted();
    Set<Variable> getWorkVariables();
    String getProgramDisplay();
    List<Variable> getInputAndWorkVariablesSortedBySerial();
    List<Label> getLabelsInProgram();
    Map<Label, Instruction> getLabelToInstruction();

    Program cloneProgram(Path xmlPath, int nextLabelNumber, int nextWorkVariableNumber) throws EngineLoadException;
    //void cloneLabels(ProgramImpl originalProgram, Map<Label, Label> oldToNewLabels);
    //void cloneVariables(ProgramImpl program, Map<Variable, Variable> oldToNewVariable);
    //void cloneInstructions(ProgramImpl program, Map<Variable, Variable> oldToNewVariable, Map<Label, Label> oldToNewLabels, Map<Instruction, Instruction> oldToNewInstructions);
    //void cloneMapLabelToInstructions(ProgramImpl program, Map<Label, Label> oldToNewLabels, Map<Instruction, Instruction> oldToNewInstructions);
    void addInstruction(Instruction instruction);
    void validateProgram() throws EngineLoadException;
    int calculateProgramMaxDegree();
    void expandProgram(int degree);
    void initialize();
    Label generateUniqueLabel();
    Variable generateUniqueVariable();
    void sortVariableSetByNumber(Set<Variable> variables);
    void addInputVariable(Variable variable);
    String getExtendedProgramDisplay();

}
