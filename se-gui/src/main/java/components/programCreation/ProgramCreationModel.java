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
    private InstructionDTO buildUnaryInstruction(String name,
                                                 int instructionNumber,
                                                 String targetVar,
                                                 String targetLabel,
                                                 String rhsSuffix) {

        // Build the command string, e.g. "x1 <- x1 + 1" / "x1 <- x1 - 1" / "x1 <- x1"
        String command = targetVar + " <- " + targetVar + (rhsSuffix == null || rhsSuffix.isBlank() ? "" : " " + rhsSuffix);

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                name,
                instructionNumber,
                InstructionDataMapper.getCyclesOfInstruction(name),
                "B",            // instruction type (kept as in your code)
                targetLabel,    // labelStr
                null,           // referenceLabelStr
                targetVar,      // targetVariableStr
                null,           // sourceVariableStr
                command,
                originDTO
        );
    }

    public InstructionDTO createIncrease(int instructionNumber, String targetVar, String targetLabel) {
        return buildUnaryInstruction("INCREASE", instructionNumber, targetVar, targetLabel, "+ 1");
    }

    public InstructionDTO createDecrease(int instructionNumber, String targetVar, String targetLabel) {
        return buildUnaryInstruction("DECREASE", instructionNumber, targetVar, targetLabel, "- 1");
    }

    public InstructionDTO createNoOp(int instructionNumber, String targetVar, String targetLabel) {
        return buildUnaryInstruction("NO_OP", instructionNumber, targetVar, targetLabel, "");
    }

    public InstructionDTO createJnz(int instructionNumber,
                                    String targetVarStr,
                                    String targetLabel,
                                    String referenceLabel) {

        String command = "IF " + targetVarStr + " != 0 GOTO " + referenceLabel;

        InstructionDTO originDTO = engine.createOriginalInstruction();

        return new InstructionDTO(
                "JNZ",
                instructionNumber,
                InstructionDataMapper.getCyclesOfInstruction("JNZ"),
                "B",
                targetLabel,      // labelStr
                referenceLabel,   // referenceLabelStr
                targetVarStr,     // targetVariableStr
                null,             // sourceVariableStr
                command,
                originDTO
        );
    }

}
