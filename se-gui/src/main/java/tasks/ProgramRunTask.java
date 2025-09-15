package tasks;

import components.debuggerExecutionMenu.RunMode;
import dto.ProgramExecutorDTO;
import engine.Engine;
import javafx.concurrent.Task;

public class ProgramRunTask extends Task<ProgramExecutorDTO> {

    private final Engine engine;
    private final RunMode runMode;
    private final Long[] inputs;
    private final int degree;

    public ProgramRunTask(Engine engine, RunMode runMode, int degree, Long[] inputs) {
        this.engine = engine;
        this.runMode = runMode;
        this.inputs = inputs;
        this.degree = degree;
    }

    @Override
    protected ProgramExecutorDTO call() {
        if (runMode == RunMode.RUNNING) {
            engine.runProgram(degree, inputs);
            return engine.getProgramAfterRun();
        }
        else if (runMode == RunMode.DEBUGGING) {
            // TODO
        }

        return null; // TODO;
    }
}
