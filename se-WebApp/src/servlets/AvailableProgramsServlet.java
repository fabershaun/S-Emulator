package servlets;

import dto.v3.AvailableProgramsDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;

import static constants.Constants.*;

@WebServlet(name = AVAILABLE_PROGRAMS_LIST_NAME, urlPatterns = {AVAILABLE_PROGRAMS_LIST_URL})
public class AvailableProgramsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Engine engine = ServletUtils.getEngine(getServletContext());
        List<AvailableProgramsDTO> availableProgramsDTOsList = engine.getAvailableMainProgramsDTOsList();

        String json = GSON_INSTANCE.toJson(availableProgramsDTOsList);
        response.getWriter().write(json);
    }
}