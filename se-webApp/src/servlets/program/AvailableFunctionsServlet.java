package servlets.program;

import dto.v3.FunctionDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.util.List;

import static utils.Constants.*;

@WebServlet(name = AVAILABLE_FUNCTIONS_LIST_NAME, urlPatterns = {AVAILABLE_FUNCTIONS_LIST_URL})
public class AvailableFunctionsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());
        List<FunctionDTO> availableProgramsDTOsList = engine.getAvailableFunctionsDTOsList();

        String json = GSON_INSTANCE.toJson(availableProgramsDTOsList);
        response.getWriter().write(json);
    }
}
