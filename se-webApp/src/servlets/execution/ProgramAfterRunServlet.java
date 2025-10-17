package servlets.execution;

import dto.v2.ProgramExecutorDTO;
import engine.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ProgramExecutionManager;
import service.ProgramRunState;
import service.ProgramRunStatus;
import utils.ServletUtils;
import java.io.IOException;
import static utils.Constants.*;
import static utils.ValidationUtils.*;

@WebServlet(name = PROGRAM_AFTER_RUN_NAME, urlPatterns = PROGRAM_AFTER_RUN_URL)
public class ProgramAfterRunServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            String runId = request.getParameter(RUN_ID_QUERY_PARAM);
            if (!validateRunIdParam(runId, response)) return;

            ProgramExecutionManager manager = ProgramExecutionManager.getInstance();
            ProgramRunStatus status = manager.getStatus(runId);

            if (status == null) {
                writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown runId", "No matching execution found for this ID");
                return;
            }

            if (status.state == ProgramRunState.FAILED) {
                // Execution has completed but ended in failure
                writeJsonError(response, HttpServletResponse.SC_CONFLICT,
                        "Program execution failed", status.error);
                return;
            }

            if (status.state != ProgramRunState.DONE) {
                // Execution is still running or pending
                writeJsonError(response, HttpServletResponse.SC_CONFLICT,
                        "Program not finished yet", "Run is still in progress");
                return;
            }

            // Program is marked DONE —> retrieve its results safely
            ProgramExecutorDTO programAfterRun;
            try {
                programAfterRun = engine.getProgramAfterRunV3(status.username);
            } catch (IllegalStateException e) {
                // History not yet updated due to short timing gap, retry on next poll
                writeJsonError(response, HttpServletResponse.SC_CONFLICT,
                        "Program not finished yet", "Result not persisted yet; please retry shortly");
                return;
            }

            if (programAfterRun == null) {
                writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Program after run unavailable", "Possible internal error");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(programAfterRun));


        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error while retrieving program result", e.getMessage());
        }
    }
}
