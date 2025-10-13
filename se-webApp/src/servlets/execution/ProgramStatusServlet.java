package servlets.execution;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ProgramExecutionManager;
import service.ProgramRunStatus;
import java.io.IOException;
import static utils.Constants.*;
import static utils.ValidationUtils.*;

/**
 * Handles GET /program-status requests.
 * Returns the current status of a running program by its runId.
 */

@WebServlet(name = PROGRAM_STATUS_NAME, urlPatterns = PROGRAM_STATUS_URL)
public class ProgramStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            if (!validateUserSession(request, response)) return;

            String runId = request.getParameter(RUN_ID_QUERY_PARAM);
            if (!validateRunIdParam(runId, response)) return;

            ProgramRunStatus status = ProgramExecutionManager.getInstance().getStatus(runId);

            if (status == null) {
                writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Run ID not found");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(GSON_INSTANCE.toJson(status));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while fetching program status", e.getMessage());
        }
    }
}
