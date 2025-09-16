package loader;

import exceptions.EngineLoadException;
import instruction.Instruction;
import instruction.synthetic.QuoteInstruction;
import program.Program;
import generatedFromXml.SProgram;
import jakarta.xml.bind.*;
import program.ProgramImpl;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class XmlProgramLoader {

    private static final String JAXB_PACKAGE = "generatedFromXml";
    private static final JAXBContext JAXB_CTX;

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(JAXB_PACKAGE);
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public Program load(Path xmlPath) throws EngineLoadException {
        validatePath(xmlPath);
        SProgram sProgram = unmarshal(xmlPath);
        Program program = XmlProgramMapper.map(sProgram);

        validateFunctions(program);
        return program;
    }

    private void validateFunctions(Program program) throws EngineLoadException {
        // Collect all defined function names
        Set<String> definedFunctions = new HashSet<>();

        if (program instanceof ProgramImpl progImpl) {
            definedFunctions.addAll(
                    progImpl.getFunctions()
                            .stream()
                            .map(func -> func.getName().toUpperCase(Locale.ROOT)) // normalize to uppercase
                            .toList()
            );

            // Validate main program instructions
            validateFunctionCalls(program, definedFunctions, "Main Program");

            // Validate each sub-function's instructions
            for (Program function : progImpl.getFunctions()) {
                function.validateProgram();                     // Validate that there are no undefined label references
                validateFunctionCalls(function, definedFunctions, "Function: " + function.getName());
            }
        } else {
            validateFunctionCalls(program, definedFunctions, "Program");
        }
    }

    private void validateFunctionCalls(Program program, Set<String> definedFunctions, String context) throws EngineLoadException {
        for (Instruction instr : program.getInstructionsList()) {
            if (instr instanceof QuoteInstruction quoteInstruction) {
                String calledFunc = quoteInstruction.getFunctionName().toUpperCase(Locale.ROOT); // assuming getter exists
                if (!definedFunctions.contains(calledFunc)) {
                    throw new EngineLoadException(
                            "Invalid function call in " + context + " (" + program.getName() + "): " +
                                    "function '" + calledFunc + "' is not defined in the XML file."
                    );

                }
            }
        }
    }

    private void validatePath(Path path) throws EngineLoadException {
        if (path == null) throw new EngineLoadException("Path is null");
        Path abs = path.toAbsolutePath().normalize();

        if (!Files.exists(abs) || !Files.isRegularFile(abs))
            throw new EngineLoadException("File not found at path: " + abs);

        if (!Files.isReadable(abs))
            throw new EngineLoadException("File is not readable: " + abs);

        if (!abs.toString().toLowerCase().endsWith(".xml"))
            throw new EngineLoadException("File must end with .xml");
    }

    private SProgram unmarshal(Path path) throws EngineLoadException {
        Path abs = path.toAbsolutePath().normalize();
        try (FileInputStream fis = new FileInputStream(abs.toFile())) {
            Unmarshaller um = JAXB_CTX.createUnmarshaller();

            StreamSource source = new StreamSource(fis);
            source.setSystemId(abs.toUri().toString());

            Object root = um.unmarshal(source);

            if (root instanceof SProgram) return (SProgram) root;
            if (root instanceof JAXBElement<?> je && je.getValue() instanceof SProgram sp) return sp;

            throw new EngineLoadException("Unexpected root element for file: " + abs);
        } catch (JAXBException e) {
            throw new EngineLoadException("Failed to parse XML: " + abs + ", " + e.getMessage(), e);
        } catch (Exception e) {
            throw new EngineLoadException("Failed to read XML: " + abs + ", " + e.getMessage(), e);
        }
    }
}
