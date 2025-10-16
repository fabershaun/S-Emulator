package engine;

import dto.v2.DebugDTO;
import dto.v2.InstructionDTO;
import dto.v2.ProgramDTO;
import dto.v2.ProgramExecutorDTO;
import dto.v3.*;
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
    Set<UserDTO> getAllUsers();
    boolean isUserExists(String username);
    UserDTO getUserDTO(String username);
    void addCreditsToUser(String username, long amountToAdd);

    List<ArchitectureDTO> getArchitectures();
    long getArchitectureCost(String architectureStr);

    String getProgramNameByUserString(String userString);
    ProgramDTO getProgramDTOByName(String programName);
    ProgramDTO getProgramDTOByUserString(String userString);
    ProgramDTO getExpandedProgramDTO(String programName, int degree);
    int getMaxDegree(String programName);

    // V2:
    ProgramExecutorDTO getProgramAfterRunV2(String programName);
    List<ProgramExecutorDTO> getHistoryV2PerProgram(String programName);    // Version 2
    List<ProgramDTO> getAllPrograms();    // V2 (to recalculateOptions())

    // V3:
    ProgramExecutorDTO getProgramAfterRunV3(String programName);
    List<HistoryRowV3DTO> getHistoryV3PerProgram(String username);           // Version 3
    List<MainProgramDTO> getAvailableMainProgramsDTOsList();
    List<FunctionDTO> getAvailableFunctionsDTOsList();

    // Expansion:
    void calculateExpansionForAllLoadedPrograms(String mainProgramName);

    // Load
    String loadProgramFromStream(InputStream xmlStream, String sourceName, String uploaderName) throws EngineLoadException;
    String loadProgramFromFile(Path path, String uploaderName) throws EngineLoadException;

    // Run
    void runProgram(String programName, String architectureTypeRepresentation, int degree, String uploaderName, Long... inputs);

    // Debug
    void initializeDebugger(String programName, String architectureTypeRepresentation, int degree, List<Long> inputs, String uploaderName);
    DebugDTO getProgramAfterStepOver(String uploaderName);
    DebugDTO getProgramAfterResume(List<Boolean> breakPoints, String uploaderName) throws InterruptedException;
    DebugDTO getProgramAfterStepBack(String uploaderName);
    void stopDebugPress(String uploaderName);

    InstructionDTO createOriginalInstruction();
    void exportToXml(File file, String programName, List<InstructionDTO> instructions);

    // For console module only:
    ProgramDTO getMainProgramToConsoleModule();
//    void loadState(Path path) throws EngineLoadException;
//    void saveState(Path path) throws EngineLoadException;


}

