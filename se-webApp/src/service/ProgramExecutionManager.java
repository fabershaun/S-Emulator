package service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProgramExecutionManager implements ExecutionService {

    private final ExecutorService threadPool;   // Thread pool for background executions
    private final Map<String, ProgramRunStatus> runStatusMap = new ConcurrentHashMap<>();   // Holds status of each program run by runId
    private static final ProgramExecutionManager instance = new ProgramExecutionManager();  // Singleton instance (optional, ensures one manager in the whole server)


    // Private constructor - initialize thread pool here
    private ProgramExecutionManager() {
        this.threadPool = Executors.newFixedThreadPool(1);
    }

    public static ProgramExecutionManager getInstance() {
        return instance;
    }

    @Override
    public String submitRun(ProgramRunRequest request) {
        // 1. Generate unique run ID
        String runId = UUID.randomUUID().toString();    // Universally Unique Identifier

        // 2. Create status object and put it in the map
        ProgramRunStatus programRunStatus = new ProgramRunStatus(runId, request.programName, request.username);
        runStatusMap.put(runId, programRunStatus);

        // 3. Submit background task
        threadPool.submit(() -> {
            try {
                // Update state to RUNNING
                programRunStatus.state = ProgramRunState.RUNNING;



                // Update state to DONE
                programRunStatus.state = ProgramRunState.DONE;

            } catch (Exception e) {
                programRunStatus.state = ProgramRunState.FAILED;
                programRunStatus.error = e.getMessage();
            }
        });

        // 4. Return run ID immediately
        return runId;
    }

    @Override
    public ProgramRunStatus getStatus(String runId) {
        return runStatusMap.get(runId);              // Return the current status if exists, otherwise null
    }

    @Override
    public boolean cancelRun(String runId) {
        return false;
    }
}
