package engine;

import dto.v2.DebugDTO;
import dto.v2.InstructionDTO;
import dto.v2.ProgramDTO;
import dto.v2.ProgramExecutorDTO;
import dto.v3.UserDTO;
import engine.logic.exceptions.EngineLoadException;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface Engine {

    // Users:
    void addUser(String username);
    void removeUser(String username);
    Set<String> getUsers();
    boolean isUserExists(String username);
    UserDTO getUserDTO(String username);

    String getProgramNameByUserString(String userString);

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

    String loadProgramFromStream(InputStream xmlStream, String sourceName, String uploaderName) throws EngineLoadException;
    String loadProgramFromFile(Path path, String uploaderName) throws EngineLoadException;
    void runProgram(String programName, int degree, String uploaderName, Long... inputs);
    void initializeDebugger(String programName, int degree, List<Long> inputs, String uploaderName);

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

