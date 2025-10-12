package servlets;

import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.Map;

import static constants.Constants.*;

@WebServlet(name = MAX_DEGREE_NAME, urlPatterns = {MAX_DEGREE_URL})
public class MaxDegreeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

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

            int maxDegree = engine.getMaxDegree(programName);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(String.valueOf(maxDegree));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON_INSTANCE.toJson("Server error: " + e.getMessage()));
        }
    }

}
