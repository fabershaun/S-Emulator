package instruction;

import dto.InstructionDTO;
import label.Label;
import label.FixedLabel;
import program.Program;
import variable.Variable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public abstract class AbstractInstruction implements Instruction, Serializable {

    private final InstructionData instructionData;
    private final InstructionType instructionType;
    private final int instructionNumber;
    private final Label label;
    private final Variable targetVariable;
    private final Instruction origin;
    private Program programOfThisInstruction = null;


    protected AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable targetVariable, Instruction origin, int instructionNumber) {
        this(instructionData, instructionType, targetVariable,FixedLabel.EMPTY, origin, instructionNumber);
    }

    protected AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable targetVariable, Label label, Instruction origin, int instructionNumber) {
        this.instructionData = instructionData;
        this.instructionType = instructionType;
        this.targetVariable = targetVariable;
        this.label = label;
        this.origin = origin;
        this.instructionNumber = instructionNumber;
    }

    @Override
    public String getName() {
        return this.instructionData.getName();
    }

    public String getInstructionType() {
        return instructionType.getInstructionType();
    }

    @Override
    public Label getLabel() {
        return this.label;
    }

    @Override
    public Label getReferenceLabel() { return null; }

    @Override
    public Variable getTargetVariable() {
        return this.targetVariable;
    }

    @Override
    public Variable getSourceVariable() {
        return null;
    }

    @Override
    public int getInstructionNumber() {
        return this.instructionNumber;
    }

    @Override
    public int getCycleOfInstruction() {
        return instructionData.getCycles();
    }

    @Override
    public List<Instruction> getExtendedInstruction() {

        if (this instanceof SyntheticInstruction syntheticInstruction) {
            return syntheticInstruction.getInnerInstructions();
        }

        return List.of(this);   // Basic instruction -> keep as it is
    }

    public Program getProgramOfThisInstruction() {
        return programOfThisInstruction;
    }

    @Override
    public void setProgramOfThisInstruction(Program programOfThisInstruction) {
        this.programOfThisInstruction = programOfThisInstruction;
    }

    @Override
    public Instruction getOriginalInstruction() {
        return origin;
    }

    @Override
    public InstructionDTO getInstructionDTO() {
        String referenceLabelStr = getReferenceLabel() != null ? getReferenceLabel().getLabelRepresentation() : "no ref label";
        String sourceVariableStr = getSourceVariable() != null ? getSourceVariable().getRepresentation() : "no source variable";

        InstructionDTO parentDto = null;
        Instruction originalInstruction = getOriginalInstruction();
        if (originalInstruction != null && !(originalInstruction instanceof OriginOfAllInstruction)) {
            parentDto = originalInstruction.getInstructionDTO();
        }

        return new InstructionDTO(
                getName(),
                getInstructionNumber(),
                getCycleOfInstruction(),
                getInstructionType(),
                getLabel().getLabelRepresentation(),
                referenceLabelStr,
                getTargetVariable().getRepresentation(),
                sourceVariableStr,
                getCommand(),
                parentDto
        );
    }

    @Override
    public List<InstructionDTO> getInstructionExtendedList() {
        if (this instanceof OriginOfAllInstruction) {
            return Collections.emptyList();
        }

        List<InstructionDTO> ancestors =
                (getOriginalInstruction() != null)
                        ? getOriginalInstruction().getInstructionExtendedList()
                        : Collections.emptyList();

        InstructionDTO current = this.getInstructionDTO();

        if (ancestors.isEmpty()) {
            return List.of(current);
        }

        List<InstructionDTO> chain = new ArrayList<>(1 + ancestors.size());
        chain.add(current);
        chain.addAll(ancestors);
        return chain;
    }
}