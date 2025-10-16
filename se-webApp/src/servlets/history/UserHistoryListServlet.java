package servlets.history;

import dto.v3.HistoryRowV3DTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.Constants.*;
import static utils.ValidationUtils.*;

@WebServlet(name = USER_HISTORY_LIST_NAME, urlPatterns = {USER_HISTORY_LIST_URL})
public class UserHistoryListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {

            String username = request.getParameter("username");
            if (username == null || username.isEmpty()) {
                username = SessionUtils.getUsername(request);
            }
            List<HistoryRowV3DTO> userHistory = engine.getHistoryV3PerProgram(username);
            if (userHistory == null) {
                userHistory = new ArrayList<>();
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(userHistory));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while fetching user history", e.getMessage());
        }
    }
}
