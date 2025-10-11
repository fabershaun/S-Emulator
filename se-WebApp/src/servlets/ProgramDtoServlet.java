package servlets;

import dto.v2.ProgramDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

import static constants.Constants.*;


@WebServlet(name = CURRENT_PROGRAM_DATA_NAME, urlPatterns = {CURRENT_PROGRAM_DATA_URL})
public class ProgramDtoServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Verify user session
            String username = SessionUtils.getUsername(request);
            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(GSON_INSTANCE.toJson("User not logged in"));
                return;
            }

            // Extract program name
            String programName = request.getParameter(PROGRAM_NAME_QUERY_PARAM);
            if (programName == null || programName.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(GSON_INSTANCE.toJson("Missing program name"));
                return;
            }

            // Retrieve engine
            Engine engine = ServletUtils.getEngine(getServletContext());
            if (engine == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(GSON_INSTANCE.toJson("Engine not initialized"));
                return;
            }

            // Fetch program data
            ProgramDTO programDTO = engine.getProgramDTOByName(programName);
            if (programDTO == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(GSON_INSTANCE.toJson("Program not found"));
                return;
            }

            // Return JSON response
            String json = GSON_INSTANCE.toJson(programDTO);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(json);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON_INSTANCE.toJson("Internal server error: " + e.getMessage()));
        }
    }
}
