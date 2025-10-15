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


@WebServlet(name = CURRENT_PROGRAM_DATA_NAME, urlPatterns = {CURRENT_PROGRAM_DATA_URL})
public class ProgramDtoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            String programName = request.getParameter(PROGRAM_NAME_QUERY_PARAM);
            if (programName == null || programName.isEmpty()) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing program name", "");
                return;
            }

            ProgramDTO programDTO = engine.getProgramDTOByName(programName);
            if (programDTO == null) {
                writeJsonError(response, HttpServletResponse.SC_NOT_FOUND,
                        "Program not found", "No program found with the given name");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(programDTO));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while fetching program data", e.getMessage());
        }
    }
}
