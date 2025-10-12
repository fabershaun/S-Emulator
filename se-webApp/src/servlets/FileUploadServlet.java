package servlets;

import dto.v2.ProgramDTO;
import engine.Engine;
import engine.logic.exceptions.EngineLoadException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.SessionUtils;

import java.io.IOException;
import java.io.InputStream;

import static utils.Constants.*;
import static utils.ServletUtils.getEngine;


@WebServlet(name = FILE_UPLOAD_SERVLET_NAME, urlPatterns = {FILE_UPLOAD_SERVLET_URL})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class FileUploadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in. Please log in first.");
            return;
        }

        Engine engine = getEngine(getServletContext());
        Part filePart = request.getPart(XML_FILE);

        if (filePart == null || filePart.getSize() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file uploaded");
            return;
        }

        try (InputStream inputStream = filePart.getInputStream()) {
            String loadedProgramName = engine.loadProgramFromStream(
                    inputStream,
                    filePart.getSubmittedFileName(),
                    username);

            ProgramDTO loadedProgramDTO = engine.getProgramDTOByName(loadedProgramName);
            String jsonResponse = GSON_INSTANCE.toJson(loadedProgramDTO);
            response.getWriter().write(jsonResponse);

        } catch (IllegalArgumentException | EngineLoadException ex) {
            // Business or validation error: the uploaded file is invalid, duplicated, or cannot be processed
            sendJsonErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            // Unexpected error: covers all unhandled exceptions or system failures
            sendJsonErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());

        }
    }

    private void sendJsonErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(GSON_INSTANCE.toJson(message));
    }
}
