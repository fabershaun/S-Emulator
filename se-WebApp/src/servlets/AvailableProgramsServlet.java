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
import java.util.Set;

import static constants.Constants.*;

@WebServlet(name = AVAILABLE_PROGRAMS_LIST_NAME, urlPatterns = {AVAILABLE_PROGRAMS_LIST_URL})
public class AvailableProgramsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Engine engine = ServletUtils.getEngine(getServletContext());
        Set<String> programsSet = engine.getMainProgramsSetStr();   // getMainProgramsSetStr()

        List<AvailableProgramsDTO> availableProgramsDTO = programsSet.stream()
                .map(name -> new AvailableProgramsDTO("Main", name, "III")) // דוגמה
                .toList();
        String json = GSON_INSTANCE.toJson(programsSet);
        response.getWriter().write(json);
    }
}
// לשלוח את שם התוכנית
// לקבל בחזרה AvailableProgramsDTO