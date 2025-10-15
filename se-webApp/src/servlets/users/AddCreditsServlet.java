package servlets.users;

import com.google.gson.JsonObject;
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

@WebServlet(name = CHARGE_CREDITS_NAME, urlPatterns = CHARGE_CREDITS_URL)
public class AddCreditsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            String username = SessionUtils.getUsername(request);

            JsonObject jsonBody = GSON_INSTANCE.fromJson(request.getReader(), JsonObject.class);
            if (!validateJsonBody(jsonBody, response)) return;

            Long amountToAdd = jsonBody.has(CREDITS_TO_CHARGE_QUERY_PARAM)
                    ? jsonBody.get(CREDITS_TO_CHARGE_QUERY_PARAM).getAsLong()
                    : null;
            if (!validateCreditsToAdd(amountToAdd, response)) return ;

            engine.addCreditsToUser(username, amountToAdd);

            UserDTO userDTO = engine.getUserDTO(username);
            long updatedCredits = userDTO.getCurrentCredits();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(updatedCredits));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server Error", e.getMessage());
        }
    }
}
