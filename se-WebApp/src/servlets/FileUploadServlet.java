package servlets;

import engine.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

import static constants.Constants.FILE_UPLOAD_SERVLET_URL;
import static constants.Constants.XML_FILE;
import static utils.ServletUtils.getEngine;


@WebServlet(name = "UploadFileServlet", urlPatterns = {FILE_UPLOAD_SERVLET_URL})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class FileUploadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart(XML_FILE);
        Engine engine = getEngine(getServletContext());

        try (InputStream inputStream = filePart.getInputStream()) {
            engine.l
        }
    }

}
