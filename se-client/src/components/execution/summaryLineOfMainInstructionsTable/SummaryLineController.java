package components.execution.summaryLineOfMainInstructionsTable;

import dto.v2.InstructionDTO;
import dto.v2.ProgramDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

public class SummaryLineController {

    private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;

    @FXML private Label amountTotalLabel;
    @FXML private Label amountBasicLabel;
    @FXML private Label amountSyntheticLabel;
    @FXML private Label a1Label;
    @FXML private Label a2Label;
    @FXML private Label a3Label;
    @FXML private Label a4Label;


    public void setProperty(ObjectProperty<ProgramDTO> programProperty) {
        this.currentSelectedProgramProperty = programProperty;
    }

    public void initializeBindings() {
        if (currentSelectedProgramProperty == null) {
            return;
        }

        // Count all instructions
        IntegerBinding totalCountBinding = Bindings.createIntegerBinding(
                () -> computeTotalInstructionsCount(currentSelectedProgramProperty.get()), currentSelectedProgramProperty);

        // Count basic instructions (type "B")
        IntegerBinding basicCountBinding = Bindings.createIntegerBinding(
                () -> computeBasicInstructionsCount(currentSelectedProgramProperty.get()), currentSelectedProgramProperty);

        // Synthetic = total - basic
        IntegerBinding syntheticCountBinding = Bindings.createIntegerBinding(
                () -> totalCountBinding.get() - basicCountBinding.get(), totalCountBinding, basicCountBinding);

        // Architecture counts
        IntegerBinding a1CountBinding = Bindings.createIntegerBinding(
                () -> computeArchitectureCount(currentSelectedProgramProperty.get(), "A_1"), currentSelectedProgramProperty);

        IntegerBinding a2CountBinding = Bindings.createIntegerBinding(
                () -> computeArchitectureCount(currentSelectedProgramProperty.get(), "A_2"), currentSelectedProgramProperty);

        IntegerBinding a3CountBinding = Bindings.createIntegerBinding(
                () -> computeArchitectureCount(currentSelectedProgramProperty.get(), "A_3"), currentSelectedProgramProperty);

        IntegerBinding a4CountBinding = Bindings.createIntegerBinding(
                () -> computeArchitectureCount(currentSelectedProgramProperty.get(), "A_4"), currentSelectedProgramProperty);

        // Bind labels to the string bindings
        amountTotalLabel.textProperty().bind(totalCountBinding.asString());
        amountBasicLabel.textProperty().bind(basicCountBinding.asString());
        amountSyntheticLabel.textProperty().bind(syntheticCountBinding.asString());
        a1Label.textProperty().bind(a1CountBinding.asString());
        a2Label.textProperty().bind(a2CountBinding.asString());
        a3Label.textProperty().bind(a3CountBinding.asString());
        a4Label.textProperty().bind(a4CountBinding.asString());
    }

    private int computeTotalInstructionsCount(ProgramDTO program) {
        if (program == null || program.getInstructions() == null) {
            return 0;
        }

        List<InstructionDTO> list = program.getInstructions().getProgramInstructionsDtoList();
        return list == null ? 0 : list.size();
    }

    private int computeBasicInstructionsCount(ProgramDTO program) {
        if (program == null || program.getInstructions() == null) {
            return 0;
        }

        List<InstructionDTO> list = program.getInstructions().getProgramInstructionsDtoList();
        if (list == null) return 0;

        // Filter all instructions where instructionTypeStr equals "B"
        return (int) list.stream()
                .filter(instr -> "B".equals(instr.getInstructionTypeStr()))
                .count();
    }

    private int computeArchitectureCount(ProgramDTO program, String architectureType) {
        if (program == null || program.getInstructions() == null) {
            return 0;
        }

        List<InstructionDTO> list = program.getInstructions().getProgramInstructionsDtoList();
        if (list == null) return 0;

        // Count how many instructions have this architecture type
        return (int) list.stream()
                .filter(instr -> architectureType.equals(instr.getArchitecture()))
                .count();
    }

}
