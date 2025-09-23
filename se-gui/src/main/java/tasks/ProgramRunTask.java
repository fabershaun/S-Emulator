package tasks;

import components.debuggerExecutionMenu.RunMode;
import dto.ProgramExecutorDTO;
import engine.Engine;
import javafx.concurrent.Task;

public class ProgramRunTask extends Task<ProgramExecutorDTO> {

    private final Engine engine;
    private final String programToRunName;
    private final RunMode runMode;
    private final Long[] inputs;
    private final int degree;

    public ProgramRunTask(String programToRunName, Engine engine, RunMode runMode, int degree, Long[] inputs) {
        this.engine = engine;
        this.programToRunName = programToRunName;
        this.runMode = runMode;
        this.inputs = inputs;
        this.degree = degree;
    }

    @Override
    protected ProgramExecutorDTO call() {
        if (runMode == RunMode.RUNNING) {
            engine.runProgram(programToRunName, degree, inputs);
            return engine.getProgramAfterRun(programToRunName);
        }
        else if (runMode == RunMode.DEBUGGING) {
            // TODO
        }

        return null; // TODO;
    }
}
