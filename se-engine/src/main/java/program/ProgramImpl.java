package program;

import dto.InstructionDTO;
import exceptions.EngineLoadException;
import instruction.Instruction;
import instruction.LabelReferencesInstruction;
import instruction.SyntheticInstruction;
import instruction.synthetic.JumpEqualFunctionInstruction;
import instruction.synthetic.QuoteInstruction;
import label.FixedLabel;
import label.Label;
import label.LabelImpl;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public class ProgramImpl implements Program, Serializable {
    private final String programName;
    private final String userString;
    private final FunctionsHolder functions;
    private final List<Instruction> programInstructions;
    private final Set<Variable> inputVariables;
    private final Set<Variable> workVariables;
    private final Map<Label, Instruction> labelToInstruction;
    private final List<Label> labelsInProgram;  // Need it to keep the order of the labels
    private final Set<Label> labelsAddedAfterExtension;  // Need it to keep the order of the labels
    private final Set<Label> referencedLabels;

    private int nextLabelNumber = 1;
    private int nextWorkVariableNumber = 1;

    public ProgramImpl(String name, String userString, FunctionsHolder functions) {
        this.programName = name;
        this.userString = userString;
        this.functions = functions;
        this.programInstructions = new ArrayList<>();
        this.labelToInstruction = new HashMap<>();
        this.inputVariables = new LinkedHashSet<>();
        this.workVariables = new LinkedHashSet<>();
        this.labelsInProgram = new ArrayList<>();
        this.labelsAddedAfterExtension = new LinkedHashSet<>();
        this.referencedLabels  = new LinkedHashSet<>();
    }

    @Override
    public Program deepClone() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();                        // buffer in memory to hold object bytes
            try (ObjectOutputStream out = new ObjectOutputStream(bos)) {                    // serialize 'this' into the byte stream
                out.writeObject(this);
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());         // create input stream from the serialized bytes
            try (ObjectInputStream in = new ObjectInputStream(bis)) {                       // deserialize back into a new ProgramImpl object
                return (Program) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed cloning program", e);
        }
    }

    @Override
    public void initialize() {
        initNextLabelNumber();
        initNextWorkVariableNumber();

//        programInstructions.stream()
//                .filter(instruction -> instruction instanceof QuoteInstruction)
//                .map(instruction -> (QuoteInstruction) instruction)
//                .forEach(QuoteInstruction::initializeInstruction);


        programInstructions.forEach(instruction -> {
            if (instruction instanceof QuoteInstruction quoteInstruction) {
                quoteInstruction.initializeInstruction();
            } else if (instruction instanceof JumpEqualFunctionInstruction jumpEqualFunctionInstruction) {
                jumpEqualFunctionInstruction.initializeInstruction();
            }
        });
    }

    @Override
    public String getName() {
        return this.programName;
    }

    @Override
    public String getUserString() {
        return userString;
    }

    @Override
    public FunctionsHolder getFunctionsHolder() {
        return functions;
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
    public void bucketVariableByFunctionInstruction(Set<Variable> variablesList) {
        for (Variable variable : variablesList) {
            switch (variable.getType()) {
                case INPUT -> inputVariables.add(variable);
                case WORK  -> workVariables.add(variable);
            }
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
    public void validateProgram() throws EngineLoadException {
        validateLabelReferencesExist();
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

    private static final Comparator<Label> EXIT_LAST_THEN_NUMBER =
            Comparator.comparing((Label l) -> FixedLabel.EXIT.equals(l))
                    .thenComparingInt(Label::getNumber);

    @Override
    public List<String> getOrderedLabelsExitLastStr() {
        return java.util.stream.Stream
                .concat(labelsInProgram.stream(), referencedLabels.stream())
                .distinct()
                .sorted(EXIT_LAST_THEN_NUMBER)
                .map(Label::getLabelRepresentation)
                .toList();
    }

    @Override
    public List<String> getInputVariablesSortedStr() {
        return inputVariables.stream()
                .sorted(Comparator.comparingInt(Variable::getNumber))
                .map(Variable::getRepresentation)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getWorkVariablesSortedStr() {
        return workVariables.stream()
                .sorted(Comparator.comparingInt(Variable::getNumber))
                .map(Variable::getRepresentation)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstructionDTO> getInstructionDtoList() {
        List<InstructionDTO> instructionDTOList = new ArrayList<>();

        for(Instruction currInstruction : programInstructions) {
            instructionDTOList.add(currInstruction.getInstructionDTO());
        }

        return instructionDTOList;
    }

    @Override
    public List<List<InstructionDTO>> getExpandedProgram() {
        List<List<InstructionDTO>> expandedProgram = new ArrayList<>();

        for (Instruction instruction : programInstructions) {
            List<InstructionDTO> chain = instruction.getInstructionExtendedList();
            if (chain != null && !chain.isEmpty()) {
                expandedProgram.add(chain);
            }
        }

        return expandedProgram;
    }

    @Override
    public List<Label> getLabelsInProgram() {
        return labelsInProgram;
    }

    @Override
    public Set<Label> getReferenceLabelsInProgram() {
        return this.referencedLabels;
    }

    @Override
    public Map<Label, Instruction> getLabelToInstruction() {
        return labelToInstruction;
    }

    @Override
    public int calculateProgramMaxDegree() {
        int maxDegree = 0;

        for (Instruction instruction : programInstructions) {
            if(instruction instanceof SyntheticInstruction syntheticInstruction) {
                maxDegree = max(maxDegree, syntheticInstruction.getMaxDegree());
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
                if (instruction instanceof SyntheticInstruction syntheticInstruction) {
                    nextInstructionNumber = syntheticInstruction.setInnerInstructionsAndReturnTheNextOne(nextInstructionNumber);
                    newInstructionsList = instruction.getExtendedInstruction();
                }
                else {
                    Instruction cloneInstruction = instruction.createInstructionWithInstructionNumber(nextInstructionNumber);
                    newInstructionsList.add(cloneInstruction);
                    nextInstructionNumber++;
                }

                iterator.remove();                                   // Remove the old instruction
                getLabelToInstruction().remove(originalLabel);       // Remove the label from the map because we will add it again in line 239
                getLabelsInProgram().remove(originalLabel);          // Remove the label from the map because we will add it again in line 239

                for (Instruction extendedInstruction : newInstructionsList) {
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
        nextLabelNumber = max(nextLabelNumber, labelsInProgram.size() + 1);
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
        nextWorkVariableNumber = max(nextWorkVariableNumber, workVariables.size() + 1);
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

    @Override
    public List<Variable> getInputAndWorkVariablesSortedBySerial() {
        sortVariableSetByNumber(inputVariables);
        sortVariableSetByNumber(workVariables);

        List<Variable> inputAndWorkVariablesAndTheirValues = new ArrayList<>(inputVariables);
        inputAndWorkVariablesAndTheirValues.addAll(workVariables);
        return inputAndWorkVariablesAndTheirValues;
    }

    @Override
    public Variable findVariableByName(String name) {

        // Search in input variables
        for (Variable variable : getInputVariables()) {
            if (variable.getRepresentation().equals(name)) {
                return variable;
            }
        }

        // Search in work variables
        for (Variable variable : getWorkVariables()) {
            if (variable.getRepresentation().equals(name)) {
                return variable;
            }
        }

        // Search in result variable (y)
        if (VariableType.RESULT.getVariableRepresentation(0).equals(name)) {
            return getResultVariable();
        }

        return null;
    }

    @Override
    public Variable getResultVariable() {
        return Variable.RESULT;
    }
}
