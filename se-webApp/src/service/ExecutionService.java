package service;

import engine.Engine;

public interface ExecutionService {
    String submitRun(ProgramRunRequest request, Engine engine);
    ProgramRunStatus getStatus(String runId);
    boolean cancelRun(String runId);
}
