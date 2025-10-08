package engine.logic.programData.program;

import dto.v2.InstructionDTO;
import engine.logic.exceptions.EngineLoadException;
import engine.logic.programData.instruction.Instruction;
import engine.logic.programData.instruction.LabelReferencesInstruction;
import engine.logic.programData.instruction.SyntheticInstruction;
import engine.logic.programData.label.FixedLabel;
import engine.logic.programData.label.Label;
import engine.logic.programData.label.LabelImpl;
import engine.logic.programData.variable.Variable;
import engine.logic.programData.variable.VariableImpl;
import engine.logic.programData.variable.VariableType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public class ProgramImpl implements Program, Serializable {
    private final String programName;
    private final String userString;

    private final ProgramsHolder programsHolder;

    private final List<Instruction> programInstructions;
    private final Set<Variable> inputVariables;
    private final Set<Variable> workVariables;
    private final Map<Label, Instruction> labelToInstruction;
    private final List<Label> labelsInProgram;  // Need it to keep the order of the labels
    private final Set<Label> labelsAddedAfterExtension;  // Need it to keep the order of the labels
    private final Set<Label> referencedLabels;

    private int nextLabelNumber = 1;
    private int nextWorkVariableNumber = 1;

    public ProgramImpl(String name, String userString, ProgramsHolder programsHolder) {
        this.programName = name;
        this.userString = userString;
        this.programsHolder = programsHolder;
        this.programInstructions = new ArrayList<>();
        this.labelToInstruction = new HashMap<>();
        this.inputVariables = new LinkedHashSet<>();
        this.workVariables = new LinkedHashSet<>();
        this.labelsInProgram = new ArrayList<>();
        this.labelsAddedAfterExtension = new LinkedHashSet<>();
        this.referencedLabels  = new LinkedHashSet<>();
    }

    @Override
    public  Map<Integer, Program> calculateDegreeToProgram() {
        Map<Integer, Program>  degreeToProgram = new HashMap<>();
        boolean canExpandMore;
        int degree = 0;

        Program workingProgram = this.deepClone();

        do {
            degreeToProgram.put(degree, workingProgram.deepClone());
            int nextInstructionNumber = 1;
            canExpandMore = false;

            for (ListIterator<Instruction> iterator = workingProgram.getInstructionsList().listIterator(); iterator.hasNext(); ) {  // Run on working program
                Instruction instruction = iterator.next();
                Label originalLabel = instruction.getLabel();
                List<Instruction> newInstructionsList = new ArrayList<>();

                if (instruction instanceof SyntheticInstruction syntheticInstruction) {
                    nextInstructionNumber = syntheticInstruction.expandInstruction(nextInstructionNumber);
                    newInstructionsList = instruction.getExtendedInstruction();
                    canExpandMore = true;
                } else {
                    Instruction cloneInstruction = instruction.createInstructionWithInstructionNumber(nextInstructionNumber);
                    newInstructionsList.add(cloneInstruction);
                    nextInstructionNumber++;
                }

                iterator.remove();                               // Remove the old instruction
                workingProgram.getLabelToInstruction().remove(originalLabel);       // Remove the label from the map because we will add it again in line 239
                workingProgram.getLabelsInProgram().remove(originalLabel);          // Remove the label from the map because we will add it again in line 239

                for (Instruction extendedInstruction : newInstructionsList) {
                    workingProgram.updateVariableAndLabel(extendedInstruction);
                    iterator.add(extendedInstruction);          // Add the extended (inner) instruction to the list
                }
            }

            degree++;
        } while (canExpandMore);

        return degreeToProgram;
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
    public Program getFunctionByName(String functionName) {
        return programsHolder.getFunctionByName(functionName);
    }

    @Override
    public ProgramsHolder getProgramsHolder() {
        return programsHolder;
    }

    @Override
    public void addInstruction(Instruction instruction) {
        updateVariableAndLabel(instruction);
        programInstructions.add(instruction);
    }

    @Override
    public void updateVariableAndLabel(Instruction instruction) {
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

        switch (variable.type()) {
            case INPUT -> inputVariables.add(variable);
            case WORK  -> workVariables.add(variable);
        }
    }

    @Override
    public void bucketVariableByFunctionInstruction(Set<Variable> variablesList) {
        for (Variable variable : variablesList) {
            switch (variable.type()) {
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
                .sorted(Comparator.comparingInt(Variable::number))
                .map(Variable::getRepresentation)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getWorkVariablesSortedStr() {
        return workVariables.stream()
                .sorted(Comparator.comparingInt(Variable::number))
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
                .filter(v -> v.type() == VariableType.WORK)
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
        List<Variable> sorted = variablesSet.stream()
                .sorted(Comparator.comparingInt(Variable::number))
                .collect(Collectors.toList());  // Dont change

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
    public Variable getResultVariable() {
        return Variable.RESULT;
    }
}
