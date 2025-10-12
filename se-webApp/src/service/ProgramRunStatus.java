package service;

public class ProgramRunStatus {
    public final String runId;
    public final String programName;
    public final String username;
    public volatile ProgramRunState state;
    public volatile Object result;  // volatile -> to make sure the thread see the most update value
    public volatile String error;

    public ProgramRunStatus(String runId, String programName, String username) {
        this.runId = runId;
        this.programName = programName;
        this.username = username;
        this.state = ProgramRunState.PENDING;
    }
}
