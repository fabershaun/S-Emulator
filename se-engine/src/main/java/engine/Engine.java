package engine;

import dto.DebugDTO;
import dto.InstructionDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface Engine {

    String getProgramNameByUserString(String userString);

//    ProgramDTO getMainProgram();
    ProgramDTO getProgramDTOByName(String programName);
    ProgramDTO getProgramDTOByUserString(String userString);
    ProgramDTO getExpandedProgramDTO(String programName, int degree);
    ProgramExecutorDTO getProgramAfterRun(String programName);
    List<ProgramExecutorDTO> getHistoryPerProgram(String programName);

    List<ProgramDTO> getAllPrograms();
    Set<String> getMainProgramsSetStr();
    Set<String> getFunctionsSetStr();

    int getMaxDegree(String programName);
    void calculateExpansionForAllLoadedPrograms(String mainProgramName);

    String loadProgramFromStream(InputStream xmlStream, String sourceName) throws EngineLoadException;
    String loadProgramFromFile(Path path) throws EngineLoadException;
    void runProgram(String programName, int degree, Long... inputs);
    void initializeDebugger(String programName, int degree, List<Long> inputs);

    DebugDTO getProgramAfterStepOver();
    DebugDTO getProgramAfterResume(List<Boolean> breakPoints) throws InterruptedException;
    DebugDTO getProgramAfterStepBack();
    void stopDebugPress();

    InstructionDTO createOriginalInstruction();
    void exportToXml(File file, String programName, List<InstructionDTO> instructions);

    // For console module only:
//    void saveState(Path path) throws EngineLoadException;
//    void loadState(Path path) throws EngineLoadException;
    ProgramDTO getMainProgramToConsoleModule();
}

