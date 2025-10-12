package servlets;

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

@WebServlet(name = USERS_LIST_NAME, urlPatterns = {USERS_LIST_URL})
public class UsersListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Engine engine = ServletUtils.getEngine(getServletContext());

        Set<UserDTO> usersList = engine.getAllUsers();

        String json = GSON_INSTANCE.toJson(usersList);
        response.getWriter().write(json);
    }
}
