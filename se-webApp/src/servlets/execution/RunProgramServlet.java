package servlets.execution;

import com.google.gson.JsonObject;
import dto.v2.ProgramDTO;
import engine.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ProgramExecutionManager;
import service.ProgramRunRequest;
import service.ProgramRunState;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Constants.*;
import static utils.ValidationUtils.*;

@WebServlet(name = RUN_PROGRAM_NAME, urlPatterns = RUN_PROGRAM_URL)
public class RunProgramServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (!validateUserSession(request, response)) return;
        String username = SessionUtils.getUsername(request);

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        try {
            JsonObject jsonBody = parseAndValidateRequestBody(request, response);
            if (jsonBody == null) return;

            ProgramRunRequest runRequest = buildProgramRunRequest(jsonBody, username, engine, response);
            if (runRequest == null) return;

            String runId = ProgramExecutionManager.getInstance().submitRun(runRequest, engine);
            writeSuccessResponse(response, runId);

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error during program submission", e.getMessage());
            e.printStackTrace();
        }
    }

    private JsonObject parseAndValidateRequestBody(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonBody = GSON_INSTANCE.fromJson(request.getReader(), JsonObject.class);
        if (!validateJsonBody(jsonBody, response)) return null;

        if (!validateJsonStringFields(jsonBody, response,
                PROGRAM_NAME_QUERY_PARAM, ARCHITECTURE_QUERY_PARAM, DEGREE_QUERY_PARAM)) {
            return null;
        }

        return jsonBody;
    }

    private ProgramRunRequest buildProgramRunRequest(JsonObject jsonBody, String username, Engine engine, HttpServletResponse response) throws IOException {

        String programName = jsonBody.get(PROGRAM_NAME_QUERY_PARAM).getAsString();
        String architecture = jsonBody.get(ARCHITECTURE_QUERY_PARAM).getAsString();
        int degree = jsonBody.get(DEGREE_QUERY_PARAM).getAsInt();

        if (!validateProgramName(programName, response)) return null;
        if (!validateArchitecture(architecture, response)) return null;
        if (!validateDegree(degree, response)) return null;

        List<Long> inputValues = validateInputs(jsonBody, response);
        if (inputValues == null) return null;

        ProgramDTO programDTO = engine.getProgramDTOByName(programName);
        if (!validateProgramExists(programDTO, response)) return null;

        return new ProgramRunRequest(programName, degree, architecture, username, inputValues);
    }

    private void writeSuccessResponse(HttpServletResponse response, String runId) throws IOException {
        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("runId", runId);
        jsonResponse.put("state", ProgramRunState.PENDING.name());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(GSON_INSTANCE.toJson(jsonResponse));
    }
}
