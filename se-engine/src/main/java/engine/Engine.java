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
    List<ProgramExecutorDTO> getHistoryToDisplay();     // For console module
    List<ProgramExecutorDTO> getHistoryPerProgram(String programName);   // For gui module

    List<ProgramDTO> getSubProgramsOfProgram(String programName);   // For gui module //TODO: return the functions also
    //void setSubProgram(String subProgramName); // For gui module //TODO: write this function. להוסיף מבנה נתונים - תוכנית פעילה כרגע

    void loadProgram(Path path) throws EngineLoadException;
    int getMaxDegree() throws EngineLoadException;
    void runProgram(int degree, Long... inputs);
    int getCurrentDegreeAfterRun();


    // For console module only:
    int getNumberOfInputVariables();
    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}
