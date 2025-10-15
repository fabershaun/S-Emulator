package servlets.users;

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

@WebServlet(name = LOGIN_SERVLET_NAME, urlPatterns = {LOGIN_SERVLET_URL})
public class LoginJavafxServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        String usernameFromSession = SessionUtils.getUsername(request);

        try {
            // Already logged in
            if (usernameFromSession != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(GSON_INSTANCE.toJson("User already logged in"));
                return;
            }

            // Username not in session, check parameter
            String usernameFromParameter = request.getParameter(USERNAME_QUERY_PARAM);
            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing username", "No username provided in request");
                return;
            }

            usernameFromParameter = usernameFromParameter.trim();

            // synchronized on: isUserExists() and addUser()
            synchronized (engine) {
                if (engine.isUserExists(usernameFromParameter)) {
                    writeJsonError(response, HttpServletResponse.SC_CONFLICT,
                            "Username already exists",
                            "Username " + usernameFromParameter + " is already in use");
                    return;
                }

                // Create new user
                engine.addUser(usernameFromParameter);
                request.getSession(true).setAttribute(USERNAME_QUERY_PARAM, usernameFromParameter);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(GSON_INSTANCE.toJson("Login successful"));
            }

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error during login", e.getMessage());
        }
    }
}
