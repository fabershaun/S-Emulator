package servlets.users;

import dto.v3.UserDTO;
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

@WebServlet(name = FETCH_CREDITS_NAME, urlPatterns = FETCH_CREDITS_URL)
public class GetUserCreditsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        try {
            String username = SessionUtils.getUsername(request);
            UserDTO userDTO = engine.getUserDTO(username);

            if (userDTO == null) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "User not found", "No user with name: " + username);
                return;
            }

            long currentCredits = userDTO.getCurrentCredits();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(GSON_INSTANCE.toJson(currentCredits));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server Error", e.getMessage());
        }
    }
}
