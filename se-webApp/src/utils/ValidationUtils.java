package utils;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dto.v2.ProgramDTO;
import engine.Engine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static utils.Constants.GSON_INSTANCE;

public class ValidationUtils {

    public static boolean validateUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "User not logged in");
            return false;
        }
        return true;
    }

    public static boolean validateJsonBody(JsonObject jsonBody, HttpServletResponse response) throws IOException {
        if (jsonBody == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(GSON_INSTANCE.toJson("Missing or invalid JSON body"));
            return false;
        }
        return true;
    }

    public static boolean validateProgramName(String programName, HttpServletResponse response) throws IOException {
        if (programName == null || programName.isEmpty()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing program name");
            return false;
        }
        return true;
    }

    public static boolean validateArchitecture(String architecture, HttpServletResponse response) throws IOException {
        if (architecture == null || architecture.isEmpty()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing architecture");
            return false;
        }
        return true;
    }

    public static boolean validateDegree(int degree, HttpServletResponse response) throws IOException {
        if (degree < 1) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid degree value (must be >= 1)");
            return false;
        }
        return true;
    }

    public static List<Long> validateInputs(JsonObject jsonBody, HttpServletResponse response) throws IOException {
        if (jsonBody.has("inputs")) {
            try {
                Type listType = new TypeToken<List<Long>>() {}.getType();
                return GSON_INSTANCE.fromJson(jsonBody.get("inputs"), listType);
            } catch (Exception e) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid inputs format (must be list of numbers)");
                return null;
            }
        }
        return List.of();
    }

    public static boolean validateEngineNotNull(Engine engine, HttpServletResponse response) throws IOException {
        if (engine == null) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Engine not initialized");
            return false;
        }
        return true;
    }

    public static boolean validateProgramExists(ProgramDTO programDTO, HttpServletResponse response) throws IOException {
        if (programDTO == null) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "Program not found");
            return false;
        }
        return true;
    }

    private static void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(GSON_INSTANCE.toJson(message));
    }
}
