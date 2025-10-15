package servlets.execution;

import dto.v3.ArchitectureDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import java.io.IOException;
import java.util.List;
import static utils.Constants.*;
import static utils.ValidationUtils.*;

@WebServlet(name = ARCHITECTURE_TYPES_NAME, urlPatterns = ARCHITECTURE_TYPES_URL)
public class ArchitectureTypesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");
        try {
            List<ArchitectureDTO> architectureDTOList = engine.getArchitectures();
            String json = GSON_INSTANCE.toJson(architectureDTOList);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(json);
        }
        catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while fetching architectures", e.getMessage());

        }
    }
}

