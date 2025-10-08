package servlets;

import dto.ProgramExecutorDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static constants.Constants.*;

@WebServlet(name = USER_HISTORY_LIST_NAME, urlPatterns = {USER_HISTORY_LIST_URL})
public class UserHistoryListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);

        // If there is no active session, return an error as JSON
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String errorJson = GSON_INSTANCE.toJson("Error: No active session");
            response.getWriter().write(errorJson);
            return;
        }

        List<ProgramExecutorDTO> userHistory =
                (List<ProgramExecutorDTO>) session.getAttribute("userHistory");

        if (userHistory == null) {
            userHistory = new ArrayList<>();
        }

        String jsonResponse = GSON_INSTANCE.toJson(userHistory);
        response.getWriter().write(jsonResponse);
    }
}
