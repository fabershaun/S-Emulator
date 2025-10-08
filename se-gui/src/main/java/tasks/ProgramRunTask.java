package tasks;

import dto.v2.ProgramExecutorDTO;
import engine.Engine;
import javafx.concurrent.Task;

public class ProgramRunTask extends Task<ProgramExecutorDTO> {

    private final Engine engine;
    private final String programToRunName;
    private final Long[] inputs;
    private final int degree;

    public ProgramRunTask(String programToRunName, Engine engine, int degree, Long[] inputs) {
        this.engine = engine;
        this.programToRunName = programToRunName;
        this.inputs = inputs;
        this.degree = degree;
    }

    @Override
    protected ProgramExecutorDTO call() {
        engine.runProgram(programToRunName, degree, inputs);
        return engine.getProgramAfterRun(programToRunName);
    }
}
