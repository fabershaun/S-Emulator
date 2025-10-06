package utils;

import okhttp3.*;
import org.jetbrains.annotations.Nullable;

public class HttpClientUtil {

    private final static SimpleCookieManager simpleCookieManager = new SimpleCookieManager();
    private final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(simpleCookieManager)
                    .followRedirects(false)
                    .build();

    public static void removeCookiesOf(String domain) {
        simpleCookieManager.removeCookiesOf(domain);
    }

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

    public static void shutdown() {
        System.out.println("Shutting down HTTP CLIENT");
        HTTP_CLIENT.dispatcher().executorService().shutdown();
        HTTP_CLIENT.connectionPool().evictAll();
    }
}
