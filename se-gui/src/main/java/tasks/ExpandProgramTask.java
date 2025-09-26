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
    private final String ProgramName;
    private final int targetDegree;

    public ExpandProgramTask(String ProgramName, Engine engine, int targetDegree) {
        this.engine = engine;
        this.ProgramName = ProgramName;
        this.targetDegree = targetDegree;
    }

    @Override
    protected ProgramDTO call() {
        if (targetDegree == 0) {
            return engine.getProgramDTOByName(ProgramName);
        } else {
            engine.expandedProgram(ProgramName, targetDegree);
            return engine.getExpandedProgram(ProgramName);
        }
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Expansion cancelled");
    }
}
