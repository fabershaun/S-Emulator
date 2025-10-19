package servlets.program;

import dto.v3.MainProgramDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import java.io.IOException;
import java.util.List;
import static utils.Constants.*;
import static utils.ValidationUtils.*;

@WebServlet(name = AVAILABLE_PROGRAMS_LIST_NAME, urlPatterns = {AVAILABLE_PROGRAMS_LIST_URL})
public class AvailableProgramsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            List<MainProgramDTO> mainProgramDTOsList = engine.getAvailableMainProgramsDTOsList();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(mainProgramDTOsList));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while fetching available programs: " + e.getMessage());
        }
    }
}