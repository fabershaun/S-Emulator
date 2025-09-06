package engine;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.List;

public interface Engine {

    void loadProgram(Path path) throws EngineLoadException;

    int getCurrentDegree();
    ProgramDTO getProgram();
    ProgramDTO getExpandedProgram(int degree);
    ProgramExecutorDTO getProgramAfterRun();
    List<ProgramExecutorDTO> getHistoryToDisplay();

    int getMaxDegree() throws EngineLoadException;
    int getNumberOfInputVariables();
    void runProgram(int degree, Long... inputs);

    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}
