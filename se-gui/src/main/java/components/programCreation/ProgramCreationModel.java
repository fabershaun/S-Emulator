package components.programCreation;

import dto.v2.InstructionDTO;
import dto.v2.ProgramDTO;
import dto.v3.UserDTO;
import engine.Engine;
import engine.EngineImpl;
import engine.logic.exceptions.EngineLoadException;
import engine.logic.programData.instruction.InstructionDataMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class ProgramCreationModel {

    private Engine engine;

    public ProgramCreationModel() {
        resetEngine();
    }

    public void resetEngine() {
        engine = new EngineImpl();
    }

    public ProgramDTO loadProgramFromFile(Path xmlPath) throws EngineLoadException {
        String loadedProgramName = engine.loadProgramFromFile(xmlPath, UserDTO.DEFAULT_NAME);
        return engine.getProgramDTOByName(loadedProgramName);
    }

    public void saveProgramToFile(File file, String programName, List<InstructionDTO> instructions) {
        engine.exportToXml(file, programName, instructions);
    }

    // Factory for all unary operations (same target on both sides)
    private InstructionDTO buildUnaryInstruction(String name, String targetVarStr, String targetLabelStr, String rhsSuffix) {

        // Build the command string, e.g. "x1 <- x1 + 1" / "x1 <- x1 - 1" / "x1 <- x1"
        String command = targetVarStr + " <- " + targetVarStr + (rhsSuffix == null || rhsSuffix.isBlank() ? "" : " " + rhsSuffix);

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                name,
                0, // Not needed
                InstructionDataMapper.getCyclesOfInstruction(name),
                "B",            // instruction type (kept as in your code)
                targetLabelStr,    // labelStr
                null,           // referenceLabelStr
                targetVarStr,      // targetVariableStr
                null,           // sourceVariableStr
                0,              // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createIncrease(String targetVariableStr, String targetLabelStr) {
        return buildUnaryInstruction("INCREASE", targetVariableStr, targetLabelStr, "+ 1");
    }

    public InstructionDTO createDecrease(String targetVariableStr, String targetLabelStr) {
        return buildUnaryInstruction("DECREASE", targetVariableStr, targetLabelStr, "- 1");
    }

    public InstructionDTO createNoOp(String targetVariableStr, String targetLabelStr) {
        return buildUnaryInstruction("NO_OP", targetVariableStr, targetLabelStr, "");
    }

    public InstructionDTO createJnz(String targetVariableStr, String targetLabelStr, String referenceLabel) {

        String command = "IF " + targetVariableStr + " != 0 GOTO " + referenceLabel;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JNZ",
                0, // Not needed
                InstructionDataMapper.getCyclesOfInstruction("JNZ"),
                "B",
                targetLabelStr,      // labelStr
                referenceLabel,   // referenceLabelStr
                targetVariableStr,     // targetVariableStr
                null,             // sourceVariableStr
                0,              // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createZeroVariable(String targetVariableStr, String targetLabelStr) {
        String command = targetVariableStr + " <- 0";

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "ZERO_VARIABLE",
                0, // Not needed
                InstructionDataMapper.getCyclesOfInstruction("ZERO_VARIABLE"),
                "S",
                targetLabelStr,      // labelStr
                null,                // referenceLabelStr
                targetVariableStr,        // targetVariableStr
                null,                // sourceVariableStr
                0,              // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createGotoLabel(String targetLabelStr, String referenceLabel) {
        String command = "GOTO " + referenceLabel;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "GOTO_LABEL",
                0, // Not needed
                InstructionDataMapper.getCyclesOfInstruction("GOTO_LABEL"),
                "S",
                targetLabelStr,         // labelStr
                referenceLabel,         // referenceLabelStr
                "y",                   // targetVariableStr
                null,                   // sourceVariableStr
                0,                      // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createAssignment(String targetVariableStr, String referenceVariableStr, String targetLabelStr) {
        String name = "ASSIGNMENT";
        String command = targetVariableStr + " <- " + referenceVariableStr;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                name,
                0, // Not needed
                InstructionDataMapper.getCyclesOfInstruction(name),
                "S",
                targetLabelStr,   // labelStr
                null,             // referenceLabelStr
                targetVariableStr,     // targetVariableStr
                referenceVariableStr,        // sourceVariableStr
                0,              // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createConstantAssignment(String targetVariableStr, String targetLabelStr, long constantValue) {
        String command = targetVariableStr + " <- " + constantValue;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "CONSTANT_ASSIGNMENT",
                0, // Not needed
                InstructionDataMapper.getCyclesOfInstruction("CONSTANT_ASSIGNMENT"),
                "S",
                targetLabelStr,
                null,
                targetVariableStr,
                null,
                constantValue,              // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createJumpZero(String targetVariableStr, String targetLabelStr, String referenceLabelStr) {
        String command = "IF " + targetVariableStr + " = 0 GOTO " + referenceLabelStr;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JUMP_ZERO",
                0, // Not needed
                InstructionDataMapper.getCyclesOfInstruction("JUMP_ZERO"),
                "S",
                targetLabelStr,
                referenceLabelStr,
                targetVariableStr,
                null,
                0,              // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createJumpEqualConstant(String targetVariableStr, String targetLabelStr, long constantValue, String referenceLabelStr) {
        String command = "IF " + targetVariableStr + " = " + constantValue + " GOTO " + referenceLabelStr;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JUMP_EQUAL_CONSTANT",
                0, // Not needed
                InstructionDataMapper.getCyclesOfInstruction("JUMP_EQUAL_CONSTANT"),
                "B",
                targetLabelStr,
                referenceLabelStr,
                targetVariableStr,
                null,
                constantValue,              // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createJumpEqualVariable(String targetVariableStr, String targetLabelStr, String sourceVariableStr, String referenceLabelStr) {
        String command = "IF " + targetVariableStr + " = " + sourceVariableStr;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JUMP_EQUAL_VARIABLE",
                0, // Not needed
                InstructionDataMapper.getCyclesOfInstruction("JUMP_EQUAL_VARIABLE"),
                "B",
                targetLabelStr,
                referenceLabelStr,
                targetVariableStr,
                sourceVariableStr,
                0,
                command,
                originDTO
        );
    }


}
