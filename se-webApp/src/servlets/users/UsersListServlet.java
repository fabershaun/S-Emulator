package servlets.users;

import dto.v3.UserDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Set;

import static utils.Constants.*;
import static utils.ValidationUtils.validateEngineNotNull;
import static utils.ValidationUtils.writeJsonError;

@WebServlet(name = USERS_LIST_NAME, urlPatterns = {USERS_LIST_URL})
public class UsersListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        try {
            Set<UserDTO> usersList = engine.getAllUsers();

            if (usersList == null || usersList.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("[]");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(usersList));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while fetching user list", e.getMessage());
        }
    }
}
