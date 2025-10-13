package servlets.execution;

import dto.v3.ArchitectureDTO;
import engine.logic.programData.architecture.ArchitectureType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static utils.Constants.*;

@WebServlet(name = ARCHITECTURE_TYPES_NAME, urlPatterns = ARCHITECTURE_TYPES_URL)
public class ArchitectureTypesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Convert enum values to list of strings
        List<String> architectureTypeStrList = Arrays.stream(ArchitectureType.values())
                .filter(type -> type != ArchitectureType.A_0)
                .map(ArchitectureType::getArchitectureRepresentation)
                .toList();

        // Wrap in DTO
        ArchitectureDTO architectureDTO = new ArchitectureDTO(architectureTypeStrList);

        // Send as JSON response
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON_INSTANCE.toJson(architectureDTO));
    }
}

