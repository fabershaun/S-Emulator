package engine;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.List;

public interface Engine {

    ProgramDTO getProgram();
    ProgramDTO getExpandedProgram(int degree);
    ProgramExecutorDTO getProgramAfterRun();
    List<ProgramExecutorDTO> getHistoryToDisplay();

    void loadProgram(Path path) throws EngineLoadException;
    int getMaxDegree() throws EngineLoadException;
    void runProgram(int degree, Long... inputs);
    int getCurrentDegreeAfterRun();


    // For console module only:
    int getNumberOfInputVariables();
    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}
