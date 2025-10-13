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

import java.io.BufferedReader;
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

        try (BufferedReader reader = request.getReader()) {
            if (!validateUserSession(request, response)) return;

            String username = SessionUtils.getUsername(request);

            JsonObject jsonBody = GSON_INSTANCE.fromJson(reader, JsonObject.class);
            if (!validateJsonBody(jsonBody, response)) return;

            String programName = jsonBody.get(PROGRAM_NAME_QUERY_PARAM).getAsString();
            String architecture = jsonBody.get(ARCHITECTURE_QUERY_PARAM).getAsString();
            int degree = jsonBody.get(DEGREE_QUERY_PARAM).getAsInt();

            if (!validateProgramName(programName, response)) return;
            if (!validateArchitecture(architecture, response)) return;
            if (!validateDegree(degree, response)) return;

            List<Long> inputValues = validateInputs(jsonBody, response);
            if (inputValues == null) return;

            Engine engine = ServletUtils.getEngine(getServletContext());
            if (!validateEngineNotNull(engine, response)) return;

            ProgramDTO programDTO = engine.getProgramDTOByName(programName);
            if (!validateProgramExists(programDTO, response)) return;

            ProgramRunRequest runRequest = new ProgramRunRequest(
                    programName,
                    degree,
                    architecture,
                    username,
                    inputValues
            );

            String runId = ProgramExecutionManager.getInstance().submitRun(runRequest, engine);

            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("runId", runId);
            jsonResponse.put("state", ProgramRunState.PENDING.name());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(GSON_INSTANCE.toJson(jsonResponse));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error during program submission", e.getMessage());
        }
    }
}
