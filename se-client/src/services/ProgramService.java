package services;

import com.google.gson.JsonObject;
import dto.v2.ProgramDTO;
import dto.v2.ProgramExecutorDTO;
import dto.v3.ArchitectureDTO;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import utils.http.HttpClientUtil;
import utils.http.HttpResponseHandler;

import java.io.IOException;
import java.util.function.Consumer;

import static utils.Constants.*;
import static utils.http.HttpResponseHandler.handleErrorResponse;

/**
 * Handles all server interactions related to Program operations:
 * - Loading program data
 * - Jumping between degrees
 * - Fetching max degree
 * - Running program
 * - Fetching program result
 */
public class ProgramService {

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
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
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


    public void fetchProgramDataAsync(String url, Consumer<ProgramDTO> onSuccess, Consumer<String> onError) {

        // Run the HTTP call asynchronously
        HttpClientUtil.runAsync(url, null, new Callback() {
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
                response.close();
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

                try (response) {
                    int maxDegree = GSON_INSTANCE.fromJson(responseBody, Integer.class);
                    onSuccess.accept(maxDegree);
                } catch (Exception e) {
                    onError.accept("Parse error: " + e.getMessage());
                }
            }
        });
    }

    public void fetchArchitectureTypesAsync(String finalUrl,
                                            Consumer<ArchitectureDTO> onSuccess,
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

                try (response) {
                    ArchitectureDTO dto = GSON_INSTANCE.fromJson(body, ArchitectureDTO.class);
                    onSuccess.accept(dto);
                } catch (Exception e) {
                    onError.accept("Failed to parse ArchitectureDTO: " + e.getMessage());
                }
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
                        JsonObject obj = GSON_INSTANCE.fromJson(body, JsonObject.class);
                        String message = obj != null && obj.has(ERROR) ? obj.get(ERROR).getAsString() : "Program execution failed on server.";
                        String details = obj != null && obj.has(DETAILS) ? obj.get(DETAILS).getAsString() : "";
                        String fullMessage = details.isEmpty() ? message : message + " - " + details;
                        onError.accept(fullMessage);
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

                try (response) {
                    JsonObject json = GSON_INSTANCE.fromJson(body, JsonObject.class);

                    // Always require "state"
                    if (!json.has(STATE)) {
                        onError.accept("Missing 'state' field in server response.");
                        return;
                    }

                    String state = json.get(STATE).getAsString();

                    // If FAILED, surface server error/details to UI
                    if ("FAILED".equals(state)) {
                        String err = json.has(ERROR) ? json.get(ERROR).getAsString() : "Program execution failed on server.";
                        String det = json.has(DETAILS) ? json.get(DETAILS).getAsString() : "";
                        onSuccess.accept("FAILED:" + (det.isEmpty() ? err : err + " - " + det));
                        return;
                    }

                    // otherwise proceed normally
                    onSuccess.accept(state);

                } catch (Exception e) {
                    onError.accept("Failed to parse response: " + e.getMessage());
                }

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
