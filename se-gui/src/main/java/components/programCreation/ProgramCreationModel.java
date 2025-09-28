package components.programCreation;

import dto.InstructionDTO;
import engine.Engine;
import engine.EngineImpl;
import instruction.InstructionDataMapper;

import java.io.File;
import java.util.List;

public class ProgramCreationModel {

    private Engine engine;

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
                0,              // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createIncrease(int instructionNumber, String targetVariableStr, String targetLabelStr) {
        return buildUnaryInstruction("INCREASE", instructionNumber, targetVariableStr, targetLabelStr, "+ 1");
    }

    public InstructionDTO createDecrease(int instructionNumber, String targetVariableStr, String targetLabelStr) {
        return buildUnaryInstruction("DECREASE", instructionNumber, targetVariableStr, targetLabelStr, "- 1");
    }

    public InstructionDTO createNoOp(int instructionNumber, String targetVariableStr, String targetLabelStr) {
        return buildUnaryInstruction("NO_OP", instructionNumber, targetVariableStr, targetLabelStr, "");
    }

    public InstructionDTO createJnz(int instructionNumber, String targetVariableStr, String targetLabelStr, String referenceLabel) {

        String command = "IF " + targetVariableStr + " != 0 GOTO " + referenceLabel;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JNZ",
                instructionNumber,
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

    public InstructionDTO createZeroVariable(int instructionNumber, String targetVariableStr, String targetLabelStr) {
        String command = targetVariableStr + " <- 0";

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "ZERO_VARIABLE",
                instructionNumber,
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
                0,                      // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createAssignment(int instructionNumber, String targetVariableStr, String referenceVariableStr, String targetLabelStr) {
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
                0,              // Constant value
                command,
                originDTO
        );
    }

    public InstructionDTO createConstantAssignment(int instructionNumber, String targetVariableStr, String targetLabelStr, long constantValue) {
        String command = targetVariableStr + " <- " + constantValue;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "CONSTANT_ASSIGNMENT",
                instructionNumber,
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

    public InstructionDTO createJumpZero(int instructionNumber, String targetVariableStr, String targetLabelStr, String referenceLabelStr) {
        String command = "IF " + targetVariableStr + " = 0 GOTO " + referenceLabelStr;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JUMP_ZERO",
                instructionNumber,
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

    public InstructionDTO createJumpEqualConstant(int instructionNumber, String targetVariableStr, String targetLabelStr, long constantValue, String referenceLabelStr) {
        String command = "IF " + targetVariableStr + " = " + constantValue + " GOTO " + referenceLabelStr;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JUMP_EQUAL_CONSTANT",
                instructionNumber,
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

    public InstructionDTO createJumpEqualVariable(int instructionNumber, String targetVariableStr, String targetLabelStr, String sourceVariableStr, String referenceLabelStr) {
        String command = "IF " + targetVariableStr + " = " + sourceVariableStr;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JUMP_EQUAL_VARIABLE",
                instructionNumber,
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
