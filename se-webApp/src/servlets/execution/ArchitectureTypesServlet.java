package servlets.execution;

import dto.v3.ArchitectureDTO;
import engine.Engine;
import engine.logic.programData.architecture.ArchitectureType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static utils.Constants.*;
import static utils.ValidationUtils.validateEngineNotNull;
import static utils.ValidationUtils.validateUserSession;

@WebServlet(name = ARCHITECTURE_TYPES_NAME, urlPatterns = ARCHITECTURE_TYPES_URL)
public class ArchitectureTypesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        try {
            List<ArchitectureDTO> architectureDTOList = engine.getArchitectures();
            String json = GSON_INSTANCE.toJson(architectureDTOList);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(json);
        }
        catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(GSON_INSTANCE.toJson("Server error: " + e.getMessage()));
        }
    }
}

