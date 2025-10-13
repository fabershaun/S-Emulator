package utils.http;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SimpleCookieManager implements CookieJar {


    Map<String, Map<String, Cookie>> cookies = new HashMap<>();

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        String host = httpUrl.host();
        List<Cookie> cookiesPerDomain = Collections.emptyList();

        synchronized (this) {
            if (cookies.containsKey(host)) {
                cookiesPerDomain = new ArrayList<>(cookies.get(host).values());
            }
        }

        return cookiesPerDomain;
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> responseCookies) {
        String host = httpUrl.host();

        synchronized (this) {
            Map<String, Cookie> cookieMap = cookies.computeIfAbsent(host, k -> new HashMap<>());
            responseCookies
                    .stream()
                    .filter(cookie -> !cookieMap.containsKey(cookie.name()))
                    .forEach(cookie -> {
                        cookieMap.put(cookie.name(), cookie);
                    });
        }
    }

    public void removeCookiesOf(String domain) {
        synchronized (this) {
            cookies.remove(domain);
        }
    }
}
