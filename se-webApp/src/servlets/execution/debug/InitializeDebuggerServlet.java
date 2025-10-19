package servlets.execution.debug;

import com.google.gson.JsonObject;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;
import java.io.IOException;
import java.util.List;
import static utils.Constants.*;
import static utils.Constants.CHOSEN_ARCHITECTURE_STR_QUERY_PARAM;
import static utils.Constants.DEGREE_QUERY_PARAM;
import static utils.Constants.PROGRAM_NAME_QUERY_PARAM;
import static utils.ValidationUtils.*;
import static utils.ValidationUtils.validateJsonStringFields;

@WebServlet(name = INITIALIZE_DEBUGGER_NAME, urlPatterns = INITIALIZE_DEBUGGER_URL)
public class InitializeDebuggerServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;
        String username = SessionUtils.getUsername(request);

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");
        try {
            JsonObject jsonBody = GSON_INSTANCE.fromJson(request.getReader(), JsonObject.class);
            if (!validateJsonBody(jsonBody, response)) return;

            if (!validateJsonStringFields(jsonBody, response, PROGRAM_NAME_QUERY_PARAM, CHOSEN_ARCHITECTURE_STR_QUERY_PARAM, DEGREE_QUERY_PARAM)) {
                return;
            }

            String programName = jsonBody.get(PROGRAM_NAME_QUERY_PARAM).getAsString();
            String architecture = jsonBody.get(CHOSEN_ARCHITECTURE_STR_QUERY_PARAM).getAsString();
            int degree = jsonBody.get(DEGREE_QUERY_PARAM).getAsInt();

            if (!validateProgramName(programName, response)) return;
            if (!validateArchitecture(architecture, response)) return;
            if (!validateDegree(degree, response)) return;

            List<Long> inputValues = validateInputs(jsonBody, response);
            if (inputValues == null) return;

            engine.initializeDebugger(programName, architecture, degree, inputValues, username);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error while initialize debugger: " + e.getMessage());
        }

    }
}
