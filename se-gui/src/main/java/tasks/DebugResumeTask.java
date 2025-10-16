package tasks;

import dto.v2.DebugDTO;
import dto.v3.UserDTO;
import engine.Engine;
import javafx.concurrent.Task;

import java.util.List;

public class DebugResumeTask extends Task<DebugDTO> {

    private final Engine engine;
    private final List<Boolean> breakPoints;

    public DebugResumeTask(Engine engine, List<Boolean> breakPoints) {
        this.engine = engine;
        this.breakPoints = breakPoints;
    }

    @Override
    protected DebugDTO call() throws Exception {
        return engine.getProgramAfterResume(breakPoints, UserDTO.DEFAULT_NAME);
    }
}
