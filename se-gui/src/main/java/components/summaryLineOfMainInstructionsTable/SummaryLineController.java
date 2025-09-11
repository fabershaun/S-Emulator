package components.summaryLineOfMainInstructionsTable;

import components.mainApp.MainAppController;
import dto.InstructionDTO;
import dto.ProgramDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;


public class SummaryLineController {

    private ObjectProperty<ProgramDTO> currentProgramProperty;

    @FXML private Label amountTotal;
    @FXML private Label amountBasic;
    @FXML private Label amountSynthetic;


    public void setProperty(ObjectProperty<ProgramDTO> programProperty) {
        this.currentProgramProperty = programProperty;
    }

    public void initializeBindings() {
        if (currentProgramProperty == null) {
            return;
        }

        // Integer bindings that recompute counts whenever currentProgramProperty changes
        IntegerBinding totalCountBinding = Bindings.createIntegerBinding(
                () -> computeTotalInstructionsCount(currentProgramProperty.get()), currentProgramProperty);

        IntegerBinding basicCountBinding = Bindings.createIntegerBinding(
                () -> computeBasicInstructionsCount(currentProgramProperty.get()), currentProgramProperty);

        IntegerBinding syntheticCountBinding = Bindings.createIntegerBinding(
                () -> totalCountBinding.get() - basicCountBinding.get(), totalCountBinding, basicCountBinding);

        // Bind labels to the string bindings
        amountTotal.textProperty().bind(totalCountBinding.asString());
        amountBasic.textProperty().bind(basicCountBinding.asString());
        amountSynthetic.textProperty().bind(syntheticCountBinding.asString());
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
}
