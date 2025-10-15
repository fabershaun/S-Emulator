package servlets.upload;

import dto.v2.ProgramDTO;
import engine.Engine;
import engine.logic.exceptions.EngineLoadException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.ServletUtils;
import utils.SessionUtils;
import java.io.IOException;
import java.io.InputStream;
import static utils.Constants.*;
import static utils.ValidationUtils.*;


@WebServlet(name = FILE_UPLOAD_SERVLET_NAME, urlPatterns = {FILE_UPLOAD_SERVLET_URL})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class FileUploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            Part filePart = request.getPart(XML_FILE);

            if (filePart == null || filePart.getSize() == 0) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "No file uploaded", "Upload request did not include a valid XML file");
                return;
            }

            String username = SessionUtils.getUsername(request);

            try (InputStream inputStream = filePart.getInputStream()) {
                String loadedProgramName = engine.loadProgramFromStream(
                        inputStream,
                        filePart.getSubmittedFileName(),
                        username
                );

                ProgramDTO loadedProgramDTO = engine.getProgramDTOByName(loadedProgramName);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(GSON_INSTANCE.toJson(loadedProgramDTO));
            }
        } catch (IllegalArgumentException | EngineLoadException ex) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid or duplicate file", ex.getMessage());

        } catch (Exception ex) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unexpected server error while uploading file", ex.getMessage());
        }
    }
}
