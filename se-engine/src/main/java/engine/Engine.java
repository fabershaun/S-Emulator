package engine;

import exceptions.EngineLoadException;

import java.nio.file.Path;

public interface Engine {

    void loadProgram(Path path) throws EngineLoadException;

    void displayProgram();

    int getMaxDegree();
    void displayExpandedProgram(int degree) throws EngineLoadException;


    void displayUsedInputVariables();
    void runProgram(int degree, Long... inputs) throws EngineLoadException;
    int getNumberOfInputVariables();
    void displayProgramAfterRun();
    void displayHistory();

}
