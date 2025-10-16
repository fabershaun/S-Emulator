package servlets.execution.debug;

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


@WebServlet(name = STOP_DEBUGGER_NAME, urlPatterns = STOP_DEBUGGER_URL)
public class StopServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;
        String username = SessionUtils.getUsername(request);

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            engine.stopDebugPress(username);
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to stop debugger", e.getMessage());
        }
    }
}
