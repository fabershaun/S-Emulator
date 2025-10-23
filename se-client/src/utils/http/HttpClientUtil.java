package utils.http;

import utils.ui.AlertUtils;
import javafx.application.Platform;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class HttpClientUtil {

    private final static SimpleCookieManager simpleCookieManager = new SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();

    public static void runAsync(String finalUrl, @Nullable RequestBody requestBody, Callback callback) {
        Request.Builder builder = new Request.Builder()
                .url(finalUrl);

        if (requestBody != null) {
            builder.post(requestBody);
        }

        Request request = builder.build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(callback);
    }

    public static String runSync(String finalUrl, @Nullable RequestBody requestBody) throws IOException {
        Request.Builder builder = new Request.Builder().url(finalUrl);

        // POST if there's a body, otherwise GET
        if (requestBody != null) {
            builder.post(requestBody);
        }

        Request request = builder.build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + ": " + response.message());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }

            return body.string();
        }
    }

    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }

    // Close ResponseBody safely anyway
    public static String readResponseBodySafely(Response response) {
        try (ResponseBody body = response.body()) {
            if (body == null) {
                Platform.runLater(() -> AlertUtils.showError("Error", "Empty response from server"));
                return null;
            }

            return body.string();

        } catch (IOException e) {
            Platform.runLater(() -> AlertUtils.showError("Error", "Failed to read server response"));
            return null;
        }
    }
}
