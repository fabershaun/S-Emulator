package tasks;

import dto.ProgramDTO;
import engine.Engine;
import javafx.concurrent.Task;

public class GetProgramTask extends Task<ProgramDTO> {
    private final Engine engine;

    public GetProgramTask(Engine engine) {
        this.engine = engine;
    }

    @Override
    protected ProgramDTO call() throws Exception {
        return engine.getProgram();
    }
}
