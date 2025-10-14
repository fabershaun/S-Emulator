package utils;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dto.v2.ProgramDTO;
import engine.Engine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Constants.*;

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

    public static boolean validateCreditsToAdd(Long amountToAdd, HttpServletResponse response) throws IOException {

        if (amountToAdd == null) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Credits amount is null", "");
            return false;
        }

        if (amountToAdd <= 0) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid amount", "Amount must be positive");
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
        if (degree < 0) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid degree value (must be >= 0)");
            return false;
        }
        return true;
    }

    public static List<Long> validateInputs(JsonObject jsonBody, HttpServletResponse response) throws IOException {
        if (jsonBody.has(INPUTS_VALUES_QUERY_PARAM)) {
            try {
                Type listType = new TypeToken<List<Long>>() {}.getType();
                return GSON_INSTANCE.fromJson(jsonBody.get(INPUTS_VALUES_QUERY_PARAM), listType);
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

    public static boolean validateRunIdParam(String runId, HttpServletResponse response) throws IOException {
        if (runId == null || runId.isEmpty()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing runId parameter");
            return false;
        }
        return true;
    }

    private static void writeError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(GSON_INSTANCE.toJson(message));
    }

    public static void writeJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        writeJsonError(response, statusCode, message, null);
    }

    /**
     * Writes a standardized JSON error response.
     * If details is null, it will be omitted from the JSON.
     */
    public static void writeJsonError(HttpServletResponse response, int statusCode, String message, String details) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(ERROR, message);

        // Add details only if provided
        if (details != null && !details.isEmpty()) {
            errorResponse.put(DETAILS, details);
        }

        response.getWriter().write(GSON_INSTANCE.toJson(errorResponse));
    }
}
