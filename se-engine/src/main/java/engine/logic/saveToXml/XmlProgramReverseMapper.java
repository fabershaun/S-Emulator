package engine.logic.saveToXml;

import dto.v2.InstructionDTO;
import engine.logic.loadFromXml.generatedFromXml.*;
import generatedFromXml.*;
import java.util.List;

public class XmlProgramReverseMapper {
    static SProgram map(String programName, List<InstructionDTO> instructionDTOList) {
        SProgram sProgram = new SProgram();
        sProgram.setName(programName);

        // Instructions
        SInstructions sInstructions = new SInstructions();
        for (InstructionDTO instr : instructionDTOList) {
            sInstructions.getSInstruction().add(mapInstruction(instr));
        }
        sProgram.setSInstructions(sInstructions);

        return sProgram;
    }

    static SInstruction mapInstruction(InstructionDTO instructionDTO) {
        SInstruction sInstruction = new SInstruction();
        sInstruction.setName(instructionDTO.getInstructionName());
        sInstruction.setSLabel(instructionDTO.getLabelStr());
        sInstruction.setSVariable(instructionDTO.getTargetVariableStr());

        SInstructionArguments args = new SInstructionArguments();

        switch (instructionDTO.getInstructionName().toUpperCase()) {
            case "INCREASE", "DECREASE", "ZERO_VARIABLE", "NO_OP" -> {
                // No need to do extra work (don't have arguments)
            }

            case "CONSTANT_ASSIGNMENT" -> {
                SInstructionArgument constArg = new SInstructionArgument();
                constArg.setName("constantValue");
                constArg.setValue(String.valueOf(instructionDTO.getConstantValue()));
                args.getSInstructionArgument().add(constArg);
            }

            case "JNZ" -> {
                SInstructionArgument labelArg = new SInstructionArgument();
                labelArg.setName("JNLabel");
                labelArg.setValue(instructionDTO.getReferenceLabelStr());
                args.getSInstructionArgument().add(labelArg);
            }

            case "GOTO_LABEL" -> {
                SInstructionArgument labelArg = new SInstructionArgument();
                labelArg.setName("GotoLabel");
                labelArg.setValue(instructionDTO.getReferenceLabelStr());
                args.getSInstructionArgument().add(labelArg);
            }

            case "ASSIGNMENT" -> {
                SInstructionArgument srcVarArg = new SInstructionArgument();
                srcVarArg.setName("sourceVariable");
                srcVarArg.setValue(instructionDTO.getSourceVariableStr());
                args.getSInstructionArgument().add(srcVarArg);
            }

            case "JUMP_ZERO" -> {
                SInstructionArgument labelArg = new SInstructionArgument();
                labelArg.setName("JZLabel");
                labelArg.setValue(instructionDTO.getReferenceLabelStr());
                args.getSInstructionArgument().add(labelArg);
            }

            case "JUMP_EQUAL_CONSTANT" -> {
                SInstructionArgument constArg = new SInstructionArgument();
                constArg.setName("constantValue");
                constArg.setValue(String.valueOf(instructionDTO.getConstantValue()));
                args.getSInstructionArgument().add(constArg);

                SInstructionArgument labelArg = new SInstructionArgument();
                labelArg.setName("JEConstantLabel");
                labelArg.setValue(instructionDTO.getReferenceLabelStr());
                args.getSInstructionArgument().add(labelArg);
            }

            case "JUMP_EQUAL_VARIABLE" -> {
                SInstructionArgument srcVarArg = new SInstructionArgument();
                srcVarArg.setName("variableName");
                srcVarArg.setValue(instructionDTO.getSourceVariableStr());
                args.getSInstructionArgument().add(srcVarArg);

                SInstructionArgument labelArg = new SInstructionArgument();
                labelArg.setName("JEVariableLabel");
                labelArg.setValue(instructionDTO.getReferenceLabelStr());
                args.getSInstructionArgument().add(labelArg);
            }

            default -> throw new IllegalArgumentException("Unsupported instruction type: " + instructionDTO.getInstructionName().toUpperCase());
        }

        if (!args.getSInstructionArgument().isEmpty()) {
            sInstruction.setSInstructionArguments(args);
        }

        return sInstruction;
    }
}
