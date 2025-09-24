package engine;

import dto.DebugDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.List;

public interface Engine {

    ProgramDTO getMainProgram();
    ProgramDTO getProgramDTOByName(String programName);
    ProgramDTO getProgramDTOByUserString(String userString);
    ProgramDTO getExpandedProgram(String programName, int degree);
    ProgramExecutorDTO getProgramAfterRun(String programName);
    List<ProgramExecutorDTO> getHistoryPerProgram(String programName);
    List<ProgramDTO> getAllPrograms();

    void loadProgram(Path path) throws EngineLoadException;
    int getMaxDegree(String programName);
    void runProgram(String programName, int degree, Long... inputs);
    void addRunToHistory(String programName, ProgramExecutorDTO programExecutorDTO);

    void initializeDebugger(String programName, int degree, List<Long> inputs);
    DebugDTO getProgramAfterStepOver();
    DebugDTO getProgramAfterResume();
    DebugDTO getProgramAfterStepBack();

    // For console module only:
    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}

