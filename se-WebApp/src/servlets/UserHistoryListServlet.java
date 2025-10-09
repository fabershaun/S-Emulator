package servlets;

import dto.v2.ProgramExecutorDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static constants.Constants.*;

@WebServlet(name = USER_HISTORY_LIST_NAME, urlPatterns = {USER_HISTORY_LIST_URL})
public class UserHistoryListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String errorJson = GSON_INSTANCE.toJson("Error: User not logged in");
            return;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());
        List<ProgramExecutorDTO> userHistory = engine.g.getUserHistory(username);

        if (userHistory == null) {
            userHistory = new ArrayList<>();
        }

        String jsonResponse = GSON_INSTANCE.toJson(userHistory);
        response.getWriter().write(jsonResponse);
    }
}
