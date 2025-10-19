package servlets.program;

import dto.v2.ProgramDTO;
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
import static utils.ValidationUtils.writeJsonError;

@WebServlet(name = JUMP_TO_DEGREE_NAME, urlPatterns = {JUMP_TO_DEGREE_URL})
public class JumpToDegreeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            String programName = request.getParameter(PROGRAM_NAME_QUERY_PARAM);
            String degreeParam = request.getParameter(TARGET_DEGREE_QUERY_PARAM);

            if (programName == null || programName.isEmpty()) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing program name");
                return;
            }

            if (degreeParam == null || degreeParam.isEmpty()) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing target degree");
                return;
            }

            int targetDegree;
            try {
                targetDegree = Integer.parseInt(degreeParam);
            } catch (NumberFormatException e) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid target degree. Degree must be a number");
                return;
            }

            ProgramDTO programDTO = engine.getExpandedProgramDTO(programName, targetDegree);
            if (programDTO == null) {
                writeJsonError(response, HttpServletResponse.SC_NOT_FOUND,
                        "Program not found: no program matches the given name and degree");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(programDTO));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while fetching program by degree: " + e.getMessage());
        }
    }
}
