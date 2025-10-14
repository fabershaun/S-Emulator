package utils;

import engine.Engine;
import engine.EngineImpl;
import jakarta.servlet.ServletContext;

public class ServletUtils {

    private static final String ENGINE_ATTRIBUTE_NAME = "engine";

    private static final Object engineLock = new Object();

    public static Engine getEngine(ServletContext servletContext) {

        synchronized (engineLock) {
            if (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, new EngineImpl());
            }
        }
        return (Engine) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);
    }
}
