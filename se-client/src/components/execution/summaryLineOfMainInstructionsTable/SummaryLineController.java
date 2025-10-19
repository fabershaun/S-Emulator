package components.execution.summaryLineOfMainInstructionsTable;

import dto.v2.InstructionDTO;
import dto.v2.ProgramDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

public class SummaryLineController {

    private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;
    private IntegerProperty architectureRankProperty;

    @FXML private Label amountTotalLabel;
    @FXML private Label amountBasicLabel;
    @FXML private Label amountSyntheticLabel;
    @FXML private Label a1Label;
    @FXML private Label a2Label;
    @FXML private Label a3Label;
    @FXML private Label a4Label;


    public void setProperty(ObjectProperty<ProgramDTO> programProperty, IntegerProperty architectureRankProperty) {
        this.currentSelectedProgramProperty = programProperty;
        this.architectureRankProperty = architectureRankProperty;
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
                () -> computeArchitectureCount(currentSelectedProgramProperty.get(), "I"), currentSelectedProgramProperty);

        IntegerBinding a2CountBinding = Bindings.createIntegerBinding(
                () -> computeArchitectureCount(currentSelectedProgramProperty.get(), "II"), currentSelectedProgramProperty);

        IntegerBinding a3CountBinding = Bindings.createIntegerBinding(
                () -> computeArchitectureCount(currentSelectedProgramProperty.get(), "III"), currentSelectedProgramProperty);

        IntegerBinding a4CountBinding = Bindings.createIntegerBinding(
                () -> computeArchitectureCount(currentSelectedProgramProperty.get(), "IV"), currentSelectedProgramProperty);

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
                .filter(instr -> architectureType.equals(instr.getArchitectureStr()))
                .count();
    }

    public void initializeColorBindings() {
        if (architectureRankProperty == null || currentSelectedProgramProperty == null) return;

        // Listen to changes in either the selected program or the chosen architecture
        ChangeListener<Object> listener = (obs, oldVal, newVal) -> updateArchitectureLabelColors();
        architectureRankProperty.addListener(listener);
        currentSelectedProgramProperty.addListener(listener);
    }

    private void updateArchitectureLabelColors() {
        ProgramDTO program = currentSelectedProgramProperty.get();
        if (program == null) return;

        // Get the minimum architecture rank required by the current program
        int minRank = program.getMiniminRequireRank();

        // Reset all label colors before applying new ones
        Label[] labels = {a1Label, a2Label, a3Label, a4Label};
        for (Label label : labels) {
            label.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-font-weight: bold;");
        }

        // Apply colors based on rank comparison
        for (int i = 0; i < labels.length; i++) {
            int rank = i + 1;
            if (rank < minRank) {
                if (architectureRankProperty.get() < minRank) {    // Color in red only if the chosen one is not enough
                    // Architecture below the minimum requirement → red text
                    labels[i].setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold;");
                }
            } else {
                // Architecture equal or above the minimum requirement → green text
                labels[i].setStyle("-fx-background-color: transparent; -fx-text-fill: green; -fx-font-weight: bold;");
            }
        }
    }

}
