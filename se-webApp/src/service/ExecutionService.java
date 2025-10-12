package service;

public interface ExecutionService {
    String submitRun(ProgramRunRequest request);
    ProgramRunStatus getStatus(String runId);
    boolean cancelRun(String runId);
}
