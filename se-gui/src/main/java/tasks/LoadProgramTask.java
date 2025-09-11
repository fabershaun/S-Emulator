package tasks;

import dto.ProgramDTO;
import engine.Engine;
import exceptions.EngineLoadException;
import javafx.concurrent.Task;

import java.nio.file.Path;

public class LoadProgramTask extends Task<ProgramDTO> {
    private final Engine engine;
    private final Path xmlPath;

    public LoadProgramTask(Engine engine, Path xmlPath) {
        this.engine = engine;
        this.xmlPath = xmlPath;
    }

    @Override
    protected ProgramDTO  call() throws EngineLoadException, InterruptedException {
        // Step 1: basic validation and initial feedback
        updateMessage("Preparing");
        updateProgress(0, 100);

        for (int p = 0; p <= 40; p += 10) {
            if (isCancelled()) return null;
            Thread.sleep(120);
            updateProgress(p, 100);
        }

        // Step 2: load program into engine
        updateMessage("Loading");
        engine.loadProgram(xmlPath);

        if (isCancelled()) {
            return null;
        }

        // Step 3: prepare the DTO to return as the task's value
        for (int p = 50; p <= 100; p += 10) {
            if (isCancelled()) return null;
            Thread.sleep(80);
            updateProgress(p, 100);
        }

        // Step 4: finish
        updateProgress(100, 100);
        updateMessage("Done");
        return engine.getProgram();
    }

    @Override
    protected void cancelled() {
        // Provide a clear message for any UI bound to messageProperty
        updateMessage("Load cancelled");
    }
}

