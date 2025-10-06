package utils;

import engine.Engine;
import engine.EngineImpl;
import jakarta.servlet.ServletContext;
import users.UserManager;

public class ServletUtils {

    private static final String ENGINE_ATTRIBUTE_NAME = "engine";
    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";

    private static final Object engineLock = new Object();
    private static final Object userManagerLock = new Object();


    public static Engine getEngine(ServletContext servletContext) {

        synchronized (engineLock) {
            if (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, new EngineImpl());
            }
        }
        return (Engine) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);
    }

    public static UserManager getUserManager(ServletContext servletContext) {

        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }
}
