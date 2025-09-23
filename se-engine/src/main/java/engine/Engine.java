package engine;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.List;

public interface Engine {

    ProgramDTO getMainProgram();
    ProgramDTO getProgramByUserString(String userString);
    ProgramDTO getExpandedProgram(String programName, int degree);
    ProgramExecutorDTO getProgramAfterRun(String programName);
    List<ProgramExecutorDTO> getHistoryToDisplay();     // For console module
    List<ProgramExecutorDTO> getHistoryPerProgram(String programName);   // For gui module

    List<ProgramDTO> getAllPrograms();   // For gui module

    void loadProgram(Path path) throws EngineLoadException;
    int getMaxDegree() throws EngineLoadException;
    void runProgram(String programName, int degree, Long... inputs);

    // For console module only:
    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}
