package services;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dto.v2.DebugDTO;
import dto.v2.ProgramDTO;
import dto.v2.ProgramExecutorDTO;
import dto.v3.ArchitectureDTO;
import dto.v3.HistoryRowV3DTO;
import javafx.application.Platform;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.http.HttpClientUtil;
import utils.http.HttpResponseHandler;
import utils.ui.AlertUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import static utils.Constants.*;
import static utils.http.HttpResponseHandler.handleErrorResponse;

/**
 * Handles all server communication for the client side.
 * - Program operations (load, run, debug, degree management)
 * - User operations (credits, history, validation)
 * - Architecture operations (fetching architecture types)
 */
public class AppService {

    public void fetchUserHistoryAsync(String finalUrl,
                                      Consumer<List<HistoryRowV3DTO>> onSuccess,
                                      Consumer<String> onError) {

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "Fetch user history");
                    onError.accept("Server returned: " + response.code());
                    return;
                }

                // Handle cases where response isn't a JSON array
                if (!responseBody.trim().startsWith("[")) {
                    Platform.runLater(() -> AlertUtils.showError("Error", "Unexpected response format: " + responseBody));
                    return;
                }

                // Define the generic list type for deserialization
                Type listType = new TypeToken<List<HistoryRowV3DTO>>() {}.getType();
                List<HistoryRowV3DTO> historyRowV3DTOList = GSON_INSTANCE.fromJson(responseBody, listType);

                onSuccess.accept(historyRowV3DTOList);

            }
        });
    }

    public void addCreditsAsync(String finalUrl,
                                RequestBody requestBody,
                                Consumer<Long> onSuccess,
                                Consumer<String> onError) {

        HttpClientUtil.runAsync(finalUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "Add credits");
                    onError.accept("Server returned: " + response.code());
                    return;
                }

                try {
                    long updatedCredits = GSON_INSTANCE.fromJson(responseBody, Long.class);
                    onSuccess.accept(updatedCredits);
                } catch (Exception e) {
                    onError.accept("Parse error: " + e.getMessage());
                }
            }
        });
    }

    public void fetchUserCreditsAsync(String finalUrl,
                                      RequestBody requestBody,
                                      Consumer<Long> onSuccess,
                                      Consumer<String> onError) {

        HttpClientUtil.runAsync(finalUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "Get user's credits");
                    onError.accept("Server returned: " + response.code());
                    return;
                }

                try {
                    long updatedCredits =  GSON_INSTANCE.fromJson(responseBody, Long.class);
                    onSuccess.accept(updatedCredits);
                } catch (Exception e) {
                    onError.accept("Parse error: " + e.getMessage());
                }
            }
        });
    }

    public void fetchUserHasEnoughCredits(String finalUrl,
                                          RequestBody requestBody,
                                          Consumer<Boolean> onSuccess,
                                          Consumer<String> onError) {
        HttpClientUtil.runAsync(finalUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "Check enough credits");
                    onError.accept("Server returned: " + response.code());
                    return;
                }

                try {
                    boolean isEnough =  GSON_INSTANCE.fromJson(responseBody, Boolean.class);
                    onSuccess.accept(isEnough);
                } catch (Exception e) {
                    onError.accept("Parse error: " + e.getMessage());
                }

            }
        });
    }

    public void fetchProgramDataAsync(String finalUrl,
                                      Consumer<ProgramDTO> onSuccess,
                                      Consumer<String> onError) {

        // Run the HTTP call asynchronously
        HttpClientUtil.runAsync(finalUrl, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "Loading program data");
                    onError.accept("Server returned: " + response.code());
                    return;
                }

                // Parse the JSON to ProgramDTO
                ProgramDTO program = GSON_INSTANCE.fromJson(responseBody, ProgramDTO.class);
                onSuccess.accept(program);
            }
        });
    }

    public void fetchJumpDegreeAsync(String finalUrl,
                                     Consumer<ProgramDTO> onSuccess,
                                     Consumer<String> onError) {

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Failed to expand program: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "Expanding program");
                    onError.accept("Server returned code " + response.code());
                    return;
                }

                ProgramDTO expandedProgram = GSON_INSTANCE.fromJson(responseBody, ProgramDTO.class);
                onSuccess.accept(expandedProgram);
            }
        });
    }

    public void fetchMaxDegreeAsync(String finalUrl,
                                    Consumer<Integer> onSuccess,
                                    Consumer<String> onError) {

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    HttpResponseHandler.handleErrorResponse(response.code(), responseBody, "Getting max degree");
                    onError.accept("Bad response: " + response.code());
                    return;
                }

                int maxDegree = GSON_INSTANCE.fromJson(responseBody, Integer.class);
                onSuccess.accept(maxDegree);
            }
        });
    }

    public void fetchArchitectureTypesAsync(String finalUrl,
                                            Consumer<List<ArchitectureDTO>> onSuccess,
                                            Consumer<String> onError) {
        HttpClientUtil.runAsync(finalUrl, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String body = HttpClientUtil.readResponseBodySafely(response);
                if (handleBadResponse(response, body, "Fetching architecture types", onError)) return;

                Type listType = new TypeToken<List<ArchitectureDTO>>() {}.getType();
                List<ArchitectureDTO> architectures = GSON_INSTANCE.fromJson(body, listType);
                onSuccess.accept(architectures);
            }
        });
    }

    public void fetchRunProgramAsync(String finalUrl,
                                     RequestBody requestBody,
                                     Consumer<String> onSuccess,
                                     Consumer<String> onError) {

        HttpClientUtil.runAsync(finalUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String body = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    try {
                        JsonObject json  = GSON_INSTANCE.fromJson(body, JsonObject.class);
                        String message;
                        if (json != null && json.has(ERROR)) {
                            message = json.get(ERROR).getAsString();
                        } else {
                            message = "Program execution failed on server.";
                        }
                        onError.accept(message);
                    } catch (Exception e) {
                        onError.accept("Program execution failed on server.");
                    }
                    return;
                }

                if (body == null) {
                    onError.accept("Empty response body");
                    return;
                }

                try {
                    JsonObject jsonResponse = GSON_INSTANCE.fromJson(body, JsonObject.class);
                    String runId = jsonResponse.get(RUN_ID_QUERY_PARAM).getAsString();
                    onSuccess.accept(runId);
                } catch (Exception e) {
                    onError.accept("Failed to parse server response: " + e.getMessage());
                }
            }
        });
    }

    public void fetchProgramStatusAsync(String finalUrl,
                                        Consumer<String> onSuccess,
                                        Consumer<String> onError) {

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Network failure
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String body = HttpClientUtil.readResponseBodySafely(response);

                if (handleBadResponse(response, body, "Fetching program status", onError)) return;

                JsonObject json = GSON_INSTANCE.fromJson(body, JsonObject.class);

                // Always require "state"
                if (!json.has(STATE)) {
                    onError.accept("Missing 'state' field in server response.");
                    return;
                }

                String state = json.get(STATE).getAsString();

                if ("FAILED".equals(state)) {
                    String err = json.has(ERROR)
                            ? json.get(ERROR).getAsString()
                            : "Program execution failed on server.";
                    onSuccess.accept("FAILED:" + err);
                    return;
                }

                // otherwise proceed normally
                onSuccess.accept(state);
            }
        });
    }

    public void fetchProgramAfterRunAsync(String finalUrl,
                                          Consumer<ProgramExecutorDTO> onSuccess,
                                          Consumer<String> onError) {

        HttpClientUtil.runAsync(finalUrl, null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "Fetch Program Result");
                    onError.accept("Server returned error code: " + response.code());
                    return;
                }

                try (response) {
                    ProgramExecutorDTO result = GSON_INSTANCE.fromJson(responseBody, ProgramExecutorDTO.class);
                    onSuccess.accept(result);
                } catch (Exception e) {
                    onError.accept("Failed to parse program result: " + e.getMessage());
                }
            }
        });
    }

    public void initializeDebugger(String finalUrl,
                                   RequestBody requestBody,
                                   Runnable onSuccess,
                                   Consumer<String> onError) {

        HttpClientUtil.runAsync(finalUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {

                try (ResponseBody ignored = response.body()) {
                    if (!response.isSuccessful()) {
                        String responseBody = HttpClientUtil.readResponseBodySafely(response);
                        handleErrorResponse(response.code(), responseBody, "initialize debugger");
                        onError.accept("Server returned error code: " + response.code());
                        return;
                    }

                    onSuccess.run();
                } catch (Exception ex) {
                    onError.accept("UI error while entering debug mode: " + ex.getMessage());
                }
            }
        });
    }

    public void debugStepOverAsync(String finalUrl,
                                   Consumer<DebugDTO> onSuccess,
                                   Consumer<String> onError) {

        RequestBody emptyBody = RequestBody.create("", MEDIA_TYPE_JSON); // Must have body cause it doPost method

        HttpClientUtil.runAsync(finalUrl, emptyBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "debug step-over");
                    onError.accept("Server returned: " + response.code());
                    return;
                }

                DebugDTO step = GSON_INSTANCE.fromJson(responseBody, DebugDTO.class);
                onSuccess.accept(step);
            }
        });
    }

    public void debugResumeAsync(String finalUrl,
                                 List<Boolean> breakPoints,
                                 Consumer<DebugDTO> onSuccess,
                                 Consumer<String> onError) {

        JsonObject json = new JsonObject();
        json.add("breakPoints", GSON_INSTANCE.toJsonTree(breakPoints));

        RequestBody requestBody = RequestBody.create(GSON_INSTANCE.toJson(json), MEDIA_TYPE_JSON);

        HttpClientUtil.runAsync(finalUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "debug resume");
                    onError.accept("Server returned: " + response.code());
                    return;
                }

                DebugDTO result = GSON_INSTANCE.fromJson(responseBody, DebugDTO.class);
                onSuccess.accept(result);
            }
        });
    }

    public void debugStepBackAsync(String finalUrl,
                                   Consumer<DebugDTO> onSuccess,
                                   Consumer<String> onError) {

        RequestBody emptyBody = RequestBody.create("", MEDIA_TYPE_JSON); // Must have body cause it doPost method

        HttpClientUtil.runAsync(finalUrl, emptyBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                String responseBody = HttpClientUtil.readResponseBodySafely(response);

                if (!response.isSuccessful()) {
                    handleErrorResponse(response.code(), responseBody, "step back");
                    onError.accept("Server returned error: " + response.code());
                    return;
                }

                DebugDTO debugStep = GSON_INSTANCE.fromJson(responseBody, DebugDTO.class);
                onSuccess.accept(debugStep);
            }
        });
    }

    public void debugStopAsync(String finalUrl,
                               Runnable onSuccess,
                               Consumer<String> onError) {

        RequestBody emptyBody = RequestBody.create("", MEDIA_TYPE_JSON); // Must have body cause it doPost method

        HttpClientUtil.runAsync(finalUrl, emptyBody, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onError.accept("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {

                try (ResponseBody ignored = response.body()) {
                    if (!response.isSuccessful()) {
                        String responseBody = HttpClientUtil.readResponseBodySafely(response);
                        HttpResponseHandler.handleErrorResponse(response.code(), responseBody, "stop debugger");
                        onError.accept("Server returned error: " + response.code());
                        return;
                    }

                    onSuccess.run();
                } catch (Exception ex) {
                    onError.accept("UI error while entering debug mode: " + ex.getMessage());
                }
            }
        });
    }



    private boolean handleBadResponse(Response response, String body, String actionDescription, Consumer<String> onError) {
        if (body == null || response.code() != 200) {
            HttpResponseHandler.handleErrorResponse(response.code(), body, actionDescription);
            onError.accept("Bad response: " + response.code());
            response.close();
            return true; // signal handled
        }
        return false;
    }
}
