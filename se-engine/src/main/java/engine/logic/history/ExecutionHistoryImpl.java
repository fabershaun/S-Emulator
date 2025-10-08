package engine.logic.history;

import engine.logic.execution.ProgramExecutor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ExecutionHistoryImpl implements ExecutionHistory, Serializable {

    private final List<ProgramExecutor> programExecutorsHistory = new ArrayList<>();

    @Override
    public List<ProgramExecutor> getProgramsExecutions() {
        return programExecutorsHistory;
    }

    @Override
    public void addProgramToHistory(ProgramExecutor programExecutor) {
        programExecutorsHistory.add(programExecutor);
    }
}
