package tasks;

import dto.ProgramDTO;
import engine.Engine;
import exceptions.EngineLoadException;
import javafx.concurrent.Task;

/**
 * Task responsible for expanding the program to a target degree
 * without blocking the JavaFX UI thread.
 */

public class ExpandProgramTask extends Task<ProgramDTO> {

    private final Engine engine;
    private final int targetDegree;

    public ExpandProgramTask(Engine engine, int targetDegree) {
        this.engine = engine;
        this.targetDegree = targetDegree;
    }

    @Override
    protected ProgramDTO call() {
        if (targetDegree == 0) {
            return engine.getProgram();
        } else {
            return engine.getExpandedProgram(targetDegree);
        }
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Expansion cancelled");
    }
}
