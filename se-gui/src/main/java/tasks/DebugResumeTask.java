package tasks;

import dto.v2.DebugDTO;
import engine.Engine;
import javafx.concurrent.Task;

import java.util.List;

public class DebugResumeTask extends Task<DebugDTO> {

    private final Engine engine;
    private final String ProgramName;
    private final List<Boolean> breakPoints;

    public DebugResumeTask(Engine engine, String ProgramName, List<Boolean> breakPoints) {
        this.engine = engine;
        this.ProgramName = ProgramName;
        this.breakPoints = breakPoints;
    }

    @Override
    protected DebugDTO call() throws Exception {
        return engine.getProgramAfterResume(breakPoints);
    }
}
