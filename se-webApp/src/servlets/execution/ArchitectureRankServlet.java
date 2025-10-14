package servlets.execution;

import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import java.io.IOException;
import static utils.Constants.*;
import static utils.Constants.GSON_INSTANCE;
import static utils.ValidationUtils.*;


@WebServlet(name = ARCHITECTURE_RANK_NAME, urlPatterns = ARCHITECTURE_RANK_URL)
public class ArchitectureRankServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        try {
            String chosenArchitectureStr = request.getParameter(ARCHITECTURE_QUERY_PARAM);
            if (!validateArchitecture(chosenArchitectureStr, response)) return;

            int resultRank = engine.getArchitectureRank(chosenArchitectureStr);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(String.valueOf(resultRank));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON_INSTANCE.toJson("Server error: " + e.getMessage()));
        }
    }
}
