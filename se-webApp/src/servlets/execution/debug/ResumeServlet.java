package servlets.execution.debug;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dto.v2.DebugDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static utils.Constants.*;
import static utils.ValidationUtils.*;

@WebServlet(name = RESUME_DEBUGGER_NAME, urlPatterns = RESUME_DEBUGGER_URL)
public class ResumeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;
        String username = SessionUtils.getUsername(request);

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            JsonObject jsonBody = GSON_INSTANCE.fromJson(request.getReader(), JsonObject.class);
            if (!validateJsonBody(jsonBody, response)) return;

            // Parse breakpoints
            Type listType = new TypeToken<List<Boolean>>() {}.getType();
            List<Boolean> breakPoints = GSON_INSTANCE.fromJson(jsonBody.get("breakPoints"), listType);

            DebugDTO result = engine.getProgramAfterResume(breakPoints, username);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(result));

        } catch (IllegalStateException e) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while resuming debugger", e.getMessage());
        }
    }
}
