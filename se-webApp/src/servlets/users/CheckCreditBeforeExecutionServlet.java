package servlets.users;

import com.google.gson.JsonObject;
import dto.v2.ProgramDTO;
import dto.v3.UserDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.BufferedReader;
import java.io.IOException;

import static utils.Constants.*;
import static utils.ValidationUtils.*;
import static utils.ValidationUtils.writeJsonError;

@WebServlet(name = CREDIT_CHECK_NAME, urlPatterns = CREDIT_CHECK_URL)
public class CheckCreditBeforeExecutionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!validateUserSession(request, response)) return;
        String username = SessionUtils.getUsername(request);

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        try (BufferedReader reader = request.getReader()) {

            JsonObject jsonBody = GSON_INSTANCE.fromJson(reader, JsonObject.class);
            if (!validateJsonBody(jsonBody, response)) return;

            UserDTO userDTO = engine.getUserDTO(username);
            if (userDTO == null) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "User not found", "No user with name: " + username);
                return;
            }

            String programName = jsonBody.get(PROGRAM_NAME_QUERY_PARAM).getAsString();
            String chosenArchitectureStr = jsonBody.get(ARCHITECTURE_QUERY_PARAM).getAsString();

            if (!validateProgramName(programName, response)) return;
            if (!validateArchitecture(chosenArchitectureStr, response)) return;

            long currentCredits = userDTO.getCurrentCredits();
            ProgramDTO programDTO = engine.getProgramDTOByName(programName);
            long architectureCost = engine.getArchitectureCost(chosenArchitectureStr);
            long requiredCredits = programDTO.getAverageCreditCost() + architectureCost;

            boolean hasEnough = currentCredits >= requiredCredits;

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(GSON_INSTANCE.toJson(hasEnough));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server Error", e.getMessage());
        }
    }
}
