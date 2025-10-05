package servlets;

import engine.Engine;
import exceptions.EngineLoadException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

import static constants.Constants.*;
import static utils.ServletUtils.getEngine;


@WebServlet(name = FILE_UPLOAD_SERVLET_NAME, urlPatterns = {FILE_UPLOAD_SERVLET_URL})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class FileUploadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart(XML_FILE);
        Engine engine = getEngine(getServletContext());

        try (InputStream inputStream = filePart.getInputStream()) {
            engine.loadProgramFromStream(inputStream, inputStream.toString());
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (EngineLoadException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
    }
}
