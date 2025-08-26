package program;

import exceptions.EngineLoadException;
import instruction.*;
import label.FixedLabel;
import label.Label;
import label.LabelImpl;
import loader.XmlProgramLoader;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ProgramImpl implements Program {

    private final String programName;
    private final List<Instruction> programInstructions;
    private final Set<Variable> inputVariables;
    private final Set<Variable> workVariables;
    private final Map<Label, Instruction> labelToInstruction;
    private final List<Label> labelsInProgram;  // Need it to keep the order of the labels
    private final Set<Label> labelsAddedAfterExtension;  // Need it to keep the order of the labels
    private final Set<Label> referencedLabels ;

    private int nextLabelNumber = 1;
    private int nextWorkVariableNumber = 1;

    public ProgramImpl(String name) {
        this.programName = name;
        this.programInstructions = new ArrayList<>();
        this.labelToInstruction = new HashMap<>();
        this.inputVariables = new LinkedHashSet<>();
        this.workVariables = new LinkedHashSet<>();
        this.labelsInProgram = new ArrayList<>();
        this.labelsAddedAfterExtension = new LinkedHashSet<>();
        this.referencedLabels  = new LinkedHashSet<>();
    }

    // TODO:         this.labelsAddedAfterExtension = new LinkedHashSet<>();
    // TODO: understand the need , delete


    @Override
    public Program cloneProgram(Path xmlPath, int nextLabelNumber, int nextWorkVariableNumber) throws EngineLoadException {
        XmlProgramLoader loader = new XmlProgramLoader();
        Program cloned = loader.load(xmlPath);
        cloned.setNextLabelNumber(nextLabelNumber);
        cloned.setNextWorkVariableNumber(nextWorkVariableNumber);
        cloned.initialize();

        return cloned;
    }

    @Override
    public void setNextLabelNumber(int nextLabelNumber) {
        this.nextLabelNumber = nextLabelNumber;
    }

    @Override
    public void setNextWorkVariableNumber(int nextWorkVariableNumber) {
        this.nextLabelNumber = nextWorkVariableNumber;
    }

    @Override
    public int getNextLabelNumber() {
        return nextLabelNumber;
    }

    @Override
    public int getNextWorkVariableNumber() {
        return nextWorkVariableNumber;
    }

/*    public Program cloneProgram1() {
        Map<Variable, Variable> oldToNewVariable = new HashMap<>();
        Map<Label, Label> oldToNewLabels = new HashMap<>();
        Map<Instruction, Instruction> oldToNewInstructions = new HashMap<>();

        Program cloned = new ProgramImpl(this.getName());
        cloned.cloneVariables(this, oldToNewVariable);
        cloned.cloneLabels(this, oldToNewLabels);
        cloned.cloneInstructions(this, oldToNewVariable, oldToNewLabels, oldToNewInstructions);
        cloned.cloneMapLabelToInstructions(this, oldToNewLabels, oldToNewInstructions);

        return cloned;
    }



    @Override
    public void cloneLabels(ProgramImpl originalProgram, Map<Label, Label> oldToNewLabels) {
        for (Label originalLabel : originalProgram.getLabelsInProgram()) {
            Label newLabel = new LabelImpl(originalLabel.getNumber());
            oldToNewLabels.put(originalLabel, newLabel);
            this.labelsInProgram.add(newLabel);
        }

        for (Label originalLabel : originalProgram.getReferencedLabels()) {
            Label newLabel = new LabelImpl(originalLabel.getNumber());
            oldToNewLabels.put(originalLabel, newLabel);
            this.referencedLabels.add(new LabelImpl(originalLabel.getNumber()));
        }

        for (Label originalLabel : originalProgram.getLabelsAddedAfterExtension()) {
            Label newLabel = new LabelImpl(originalLabel.getNumber());
            oldToNewLabels.put(originalLabel, newLabel);
            this.labelsAddedAfterExtension.add(new LabelImpl(originalLabel.getNumber()));
        }
    }

    @Override
    public void cloneVariables(ProgramImpl program, Map<Variable, Variable> oldToNewVariable) {
        for (Variable originalVariable : program.getInputVariables()) {
            Variable newVariable = new VariableImpl(originalVariable.getType(), originalVariable.getNumber());
            oldToNewVariable.put(originalVariable, newVariable);
            this.inputVariables.add(newVariable);
        }

        for (Variable originalVariable : program.getWorkVariables()) {
            Variable newVariable = new VariableImpl(originalVariable.getType(), originalVariable.getNumber());
            oldToNewVariable.put(originalVariable, newVariable);
            this.workVariables.add(new VariableImpl(originalVariable.getType(), originalVariable.getNumber()));
        }
    }

    @Override
    public void cloneInstructions(ProgramImpl program, Map<Variable, Variable> oldToNewVariable, Map<Label, Label> oldToNewLabels, Map<Instruction, Instruction> oldToNewInstructions) {
        int nextInstructionNumber = 1;
        Instruction originInstruction = new OriginOfAllInstruction();

        for (Instruction originalInstruction : program.programInstructions) {
            Instruction newInstruction = null;

            if (originalInstruction instanceof BasicInstruction basicInstruction) {
                Variable targetVariable = oldToNewVariable.get(originalInstruction.getTargetVariable());
                Label label = oldToNewLabels.get(originalInstruction.getLabel());
                int instructionNumber = originalInstruction.getInstructionNumber();

                Label referenceLabel = null;
                if (originalInstruction instanceof LabelReferencesInstruction referencesInstruction) {
                    referenceLabel = oldToNewLabels.get(referencesInstruction.getReferenceLabel());
                }

                newInstruction = basicInstruction.cloneInstruction(targetVariable, label, referenceLabel, originInstruction, instructionNumber);
            }
            else if (originalInstruction instanceof SyntheticInstruction syntheticInstruction) {
                Variable targetVariable = oldToNewVariable.get(originalInstruction.getTargetVariable());
                Label label = oldToNewLabels.get(originalInstruction.getLabel());
                int instructionNumber = originalInstruction.getInstructionNumber();
                Label referenceLabel = null;
                if (originalInstruction instanceof LabelReferencesInstruction referencesInstruction) {
                    referenceLabel = oldToNewLabels.get(referencesInstruction.getReferenceLabel());
                }

                Variable sourceVariable = oldToNewVariable.get(originalInstruction.getSourceVariable());
                long constantValue = 0;
                if (originalInstruction instanceof ConstantInstructions constantInstruction) {
                    constantValue = constantInstruction.getConstantValue();
                }

                newInstruction = syntheticInstruction.CloneInstruction(targetVariable, label, sourceVariable, constantValue, referenceLabel, originInstruction, instructionNumber);
            }

            this.addInstruction(newInstruction);
            oldToNewInstructions.put(originalInstruction, newInstruction);
            nextInstructionNumber++;
        }
    }

    @Override
    public void cloneMapLabelToInstructions(ProgramImpl program, Map<Label, Label> oldToNewLabels, Map<Instruction, Instruction> oldToNewInstructions) {
        for (Map.Entry<Label, Instruction> entry : program.getLabelToInstruction().entrySet()) {
            Label originalLabel = entry.getKey();
            Instruction originalInstruction = entry.getValue();

            Label clonedLabel = oldToNewLabels.get(originalLabel);
            Instruction clonedInstruction = oldToNewInstructions.get(originalInstruction);

            this.labelToInstruction.put(clonedLabel, clonedInstruction);
        }
    }*/

/*    public Set<Label> getLabelsAddedAfterExtension() {
        return labelsAddedAfterExtension;
    }

    public Set<Label> getReferencedLabels() {
        return referencedLabels;
    }*/






    @Override
    public void initialize() {
        initNextLabelNumber();
        initNextWorkVariableNumber();
    }

    @Override
    public String getName() {
        return this.programName;
    }

    @Override
    public void addInstruction(Instruction instruction) {

        updateVariableAndLabel(instruction);

        programInstructions.add(instruction);
    }

    private void updateVariableAndLabel(Instruction instruction) {
        Label currentLabel = instruction.getLabel();

        bucketLabel(instruction, currentLabel);
        bucketVariable(instruction.getTargetVariable());
        bucketVariable(instruction.getSourceVariable());
    }

    private void bucketLabel(Instruction instruction, Label currentLabel) {
        if(currentLabel != FixedLabel.EMPTY) {                    // Add label and its instruction to map
            if (!labelToInstruction.containsKey(currentLabel)) {
                labelsInProgram.add(currentLabel);
                labelToInstruction.put(currentLabel, instruction);
            } else {
                throw new IllegalArgumentException(
                        "Duplicate label " + currentLabel.getLabelRepresentation() + " at instructions: " +
                                labelToInstruction.get(currentLabel).getName() + " and " + instruction.getName()
                );
            }
        }

        if (instruction instanceof LabelReferencesInstruction labelReferencesInstruction) {
            Label addedLabel = labelReferencesInstruction.getReferenceLabel();
            referencedLabels.add(addedLabel);
        }
    }

    private void bucketVariable(Variable variable) {
        if (variable == null) return;

        switch (variable.getType()) {
            case INPUT -> inputVariables.add(variable);
            case WORK  -> workVariables.add(variable);
        }
    }

    @Override
    public List<Instruction> getInstructionsList() {
        return this.programInstructions;
    }

    @Override
    public Instruction getInstructionByLabel(Label label) {
        return labelToInstruction.get(label);
    }

    @Override
    public Set<Variable> getInputVariables() {
        return this.inputVariables;
    }

    @Override
    public Set<Variable> getWorkVariables() {
        return this.workVariables;
    }

    @Override
    public String getProgramDisplay() {

        List<String> variablesInputInProgram = getInputVariableSorted();
        List<String> labels = getOrderedLabelsExitLast();

        StringBuilder programDisplay = new StringBuilder();
        programDisplay.append("Program Display:").append(System.lineSeparator());
        programDisplay.append("Name: ").append(getName()).append(System.lineSeparator());
        programDisplay.append("Inputs: ").append(String.join(", ", variablesInputInProgram)).append(System.lineSeparator());
        programDisplay.append("Labels: ").append(String.join(", ", labels)).append(System.lineSeparator());
        programDisplay.append("Instructions: ").append(System.lineSeparator());

        programDisplay.append(programRepresentation());

        return programDisplay.toString();
    }

    @Override
    public List<Variable> getInputAndWorkVariablesSortedBySerial() {
        sortVariableSetByNumber(inputVariables);
        sortVariableSetByNumber(workVariables);

        List<Variable> inputAndWorkVariablesAndTheirValues = new ArrayList<>(inputVariables);
        inputAndWorkVariablesAndTheirValues.addAll(workVariables);
        return inputAndWorkVariablesAndTheirValues;
    }

    @Override
    public List<Label> getLabelsInProgram() {
        return labelsInProgram;
    }

    @Override
    public Map<Label, Instruction> getLabelToInstruction() {
        return labelToInstruction;
    }

    @Override
    public void validateProgram() throws EngineLoadException {
        validateLabelReferencesExist();
    }

    @Override
    public List<String> getInputVariableSorted() {
        return inputVariables.stream()
                .sorted(Comparator.comparingInt(Variable::getNumber))
                .map(Variable::getRepresentation)
                .collect(Collectors.toList());
    }

    private List<String> getOrderedLabelsExitLast() {
        return labelsInProgram.stream()
                .sorted(
                        Comparator.comparing((Label l) -> FixedLabel.EXIT.equals(l))
                                .thenComparingInt(Label::getNumber)
                )
                .map(Label::getLabelRepresentation)
                .toList();
    }

    private void validateLabelReferencesExist() throws EngineLoadException {
        Set<Label> undefined = new java.util.LinkedHashSet<>(referencedLabels);
        undefined.removeAll(labelToInstruction.keySet());
        undefined.remove(FixedLabel.EXIT);

        if (!undefined.isEmpty()) {
            String names = undefined.stream()
                    .filter(lbl -> lbl != FixedLabel.EMPTY && lbl != FixedLabel.EXIT)
                    .map(Label::getLabelRepresentation)
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new EngineLoadException("Undefined label reference(s) in the program: " + names);
        }
    }

    private String programRepresentation() {
        StringBuilder programDisplay = new StringBuilder();
        int numberOfInstructionsInProgram = programInstructions.size();

        for (Instruction instruction : programInstructions) {
            String line = instruction.getInstructionRepresentation(numberOfInstructionsInProgram);
            programDisplay.append(line).append(System.lineSeparator());
        }

        return programDisplay.toString();
    }

    @Override
    public String getExtendedProgramDisplay() {
        StringBuilder extendedProgramDisplay = new StringBuilder();

        int numberOfInstructionsInProgram = programInstructions.size();

        for(Instruction instruction : programInstructions) {
            extendedProgramDisplay.append(instruction.getInstructionExtendedDisplay(numberOfInstructionsInProgram)).append(System.lineSeparator());
        }

        return extendedProgramDisplay.toString();
    }

    @Override
    public int calculateProgramMaxDegree() {
        int maxDegree = 0;

        for (Instruction instruction : programInstructions) {
            if(instruction instanceof SyntheticInstruction syntheticInstruction) {
                maxDegree = Math.max(maxDegree, syntheticInstruction.getMaxDegree());
            }
        }

        return maxDegree;
    }

    @Override
    public void expandProgram(int degree) {
        for (int i = 0 ; i < degree ; i++) {
            int nextInstructionNumber = 1;

            for (ListIterator<Instruction> iterator = getInstructionsList().listIterator(); iterator.hasNext(); ) {
                Instruction instruction = iterator.next();
                Label originalLabel = instruction.getLabel();
                List<Instruction> newInstructionsList = new ArrayList<>();

                // initialize
                instruction.setProgramOfThisInstruction(this);
                if (instruction instanceof SyntheticInstruction syntheticInstruction) {
                    nextInstructionNumber = syntheticInstruction.setInnerInstructionsAndReturnTheNextOne(nextInstructionNumber);
                    newInstructionsList = instruction.getExtendedInstruction();
                }
                else {
                    Instruction cloneInstruction = instruction.createInstructionWithInstructionNumber(nextInstructionNumber);
                    newInstructionsList.add(cloneInstruction);
                    nextInstructionNumber++;
                }

                iterator.remove();                              // Remove the old instruction
                getLabelToInstruction().remove(originalLabel);       // Remove the label from the map because we will add it again in line 239
                getLabelsInProgram().remove(originalLabel);          // Remove the label from the map because we will add it again in line 239

                for (Instruction extendedInstruction : newInstructionsList) {
                    //totalCycles += extendedInstruction.getCycleOfInstruction();
                    updateVariableAndLabel(extendedInstruction);
                    iterator.add(extendedInstruction);          // Add the extended (inner) instruction to the list
                }
            }
        }
    }

    private void initNextLabelNumber() {
        nextLabelNumber = labelsInProgram.stream()
                .map(Label::getLabelRepresentation)
                .filter(s -> s.matches("L\\d+"))
                .mapToInt(s -> Integer.parseInt(s.substring(1)))
                .max().orElse(0) + 1;
    }

    @Override
    public Label generateUniqueLabel() {
        Label uniqueLabel = new LabelImpl(nextLabelNumber++);

        if (labelsAddedAfterExtension.contains(uniqueLabel)) {
            throw new IllegalStateException(
                    "Attempted to add duplicate labels after extention: " + uniqueLabel.getLabelRepresentation()
            );
        }
        labelsAddedAfterExtension.add(uniqueLabel);
        return uniqueLabel;
    }

    private void initNextWorkVariableNumber() {
        nextWorkVariableNumber = workVariables.stream()
                .filter(v -> v.getType() == VariableType.WORK)
                .map(Variable::getRepresentation)
                .map(rep -> {
                    String digits = rep.replaceAll("\\D+", "");    // Only the digits
                    return digits.isEmpty() ? 0 : Integer.parseInt(digits);
                })
                .max(Integer::compare)
                .orElse(0) + 1;                                   // Return 1 if the set is empty
    }

    @Override
    public Variable generateUniqueVariable() {
        Variable v = new VariableImpl(VariableType.WORK, nextWorkVariableNumber++);
        workVariables.add(v);
        return v;
    }

    @Override
    public void sortVariableSetByNumber(Set<Variable> variablesSet) {
        var sorted = variablesSet.stream()
                .sorted(Comparator.comparingInt(Variable::getNumber))
                .toList();

        variablesSet.clear();
        variablesSet.addAll(sorted);
    }

    @Override
    public void addInputVariable(Variable variable) {
        inputVariables.add(variable);
    }
}
