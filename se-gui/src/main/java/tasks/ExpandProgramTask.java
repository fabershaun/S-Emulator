package tasks;

import dto.ProgramDTO;
import engine.Engine;
import javafx.concurrent.Task;

/**
 * Task responsible for expanding the program to a target degree
 * without blocking the JavaFX UI thread.
 */

public class ExpandProgramTask extends Task<ProgramDTO> {

    private final Engine engine;
    private final String programToExpandName;
    private final int targetDegree;

    public ExpandProgramTask(String programToExpandName, Engine engine, int targetDegree) {
        this.engine = engine;
        this.programToExpandName = programToExpandName;
        this.targetDegree = targetDegree;
    }

    @Override
    protected ProgramDTO call() {
        if (targetDegree == 0) {
            return engine.getProgramDTOByName(programToExpandName);
        } else {
            return engine.getExpandedProgram(programToExpandName, targetDegree);
        }
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Expansion cancelled");
    }
}
