package listeners;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import service.ProgramExecutionManager;

/**
 * Listener that runs automatically when the web application starts or stops.
 * Used to gracefully shut down background thread pools.
 */
@WebListener
public class AppShutdownListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        // Safely shut down the server-side thread pool to prevent memory leaks
        ProgramExecutionManager.getInstance().shutdown();
    }
}
