package servlets.execution.debug;

import dto.v2.DebugDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

import static utils.Constants.*;
import static utils.ValidationUtils.*;

@WebServlet(name = STEP_OVER_DEBUGGER_NAME, urlPatterns = STEP_OVER_DEBUGGER_URL)
public class StepOverServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;
        String username = SessionUtils.getUsername(request);

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            DebugDTO debugStep = engine.getProgramAfterStepOver(username);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(debugStep));

        } catch (IllegalStateException e) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while performing step-over", e.getMessage());
        }
    }
}
