package components.programCreation;

import dto.InstructionDTO;
import engine.Engine;
import engine.EngineImpl;
import instruction.InstructionDataMapper;

import java.io.File;
import java.util.List;

public class ProgramCreationModel {

    private Engine engine;
//    private InstructionDTO currentInstruction;
//    private String newProgramName;
//    private String instructionKind;
//    private String targetVariableStr;
//    private String sourceVariable;
//    private String targetLabel;
//    private String referenceLabel;
//    private String constantValue;

    public ProgramCreationModel() {
        resetEngine();
    }

    public void resetEngine() {
        engine = new EngineImpl();
    }

    public void createNewProgramInEngine(List<InstructionDTO> instructionDTOList) {
        //engine.createNewProgram(instructionDTOList);
    }

    public void saveProgramToFile(File file) {
        //engine.exportToXml(file);
    }

    // Factory for all unary operations (same target on both sides)
    private InstructionDTO buildUnaryInstruction(String name, int instructionNumber, String targetVarStr, String targetLabelStr, String rhsSuffix) {

        // Build the command string, e.g. "x1 <- x1 + 1" / "x1 <- x1 - 1" / "x1 <- x1"
        String command = targetVarStr + " <- " + targetVarStr + (rhsSuffix == null || rhsSuffix.isBlank() ? "" : " " + rhsSuffix);

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                name,
                instructionNumber,
                InstructionDataMapper.getCyclesOfInstruction(name),
                "B",            // instruction type (kept as in your code)
                targetLabelStr,    // labelStr
                null,           // referenceLabelStr
                targetVarStr,      // targetVariableStr
                null,           // sourceVariableStr
                command,
                originDTO
        );
    }

    public InstructionDTO createIncrease(int instructionNumber, String targetVarStr, String targetLabelStr) {
        return buildUnaryInstruction("INCREASE", instructionNumber, targetVarStr, targetLabelStr, "+ 1");
    }

    public InstructionDTO createDecrease(int instructionNumber, String targetVarStr, String targetLabelStr) {
        return buildUnaryInstruction("DECREASE", instructionNumber, targetVarStr, targetLabelStr, "- 1");
    }

    public InstructionDTO createNoOp(int instructionNumber, String targetVarStr, String targetLabelStr) {
        return buildUnaryInstruction("NO_OP", instructionNumber, targetVarStr, targetLabelStr, "");
    }

    public InstructionDTO createJnz(int instructionNumber, String targetVarStr, String targetLabelStr, String referenceLabel) {

        String command = "IF " + targetVarStr + " != 0 GOTO " + referenceLabel;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JNZ",
                instructionNumber,
                InstructionDataMapper.getCyclesOfInstruction("JNZ"),
                "B",
                targetLabelStr,      // labelStr
                referenceLabel,   // referenceLabelStr
                targetVarStr,     // targetVariableStr
                null,             // sourceVariableStr
                command,
                originDTO
        );
    }

    public InstructionDTO createZeroVariable(int instructionNumber, String targetVarStr, String targetLabelStr) {
        String command = targetVarStr + " <- 0";

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "ZERO_VARIABLE",
                instructionNumber,
                InstructionDataMapper.getCyclesOfInstruction("ZERO_VARIABLE"),
                "S",
                targetLabelStr,      // labelStr
                null,                // referenceLabelStr
                targetVarStr,        // targetVariableStr
                null,                // sourceVariableStr
                command,
                originDTO
        );
    }

    public InstructionDTO createGotoLabel(int instructionNumber, String targetLabelStr, String referenceLabel) {
        String command = "GOTO " + referenceLabel;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "GOTO_LABEL",
                instructionNumber,
                InstructionDataMapper.getCyclesOfInstruction("GOTO_LABEL"),
                "S",
                targetLabelStr,         // labelStr
                referenceLabel,         // referenceLabelStr
                null,                   // targetVariableStr
                null,                   // sourceVariableStr
                command,
                originDTO
        );
    }

    public InstructionDTO createAssignment(int instructionNumber,
                                           String targetVariableStr,
                                           String referenceVariableStr,
                                           String targetLabelStr) {
        String name = "ASSIGNMENT";
        String command = targetVariableStr + " <- " + referenceVariableStr;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                name,
                instructionNumber,
                InstructionDataMapper.getCyclesOfInstruction(name),
                "S",
                targetLabelStr,   // labelStr
                null,             // referenceLabelStr
                targetVariableStr,     // targetVariableStr
                referenceVariableStr,        // sourceVariableStr
                command,
                originDTO
        );
    }

}
