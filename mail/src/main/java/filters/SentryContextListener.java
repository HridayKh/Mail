package filters;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import io.sentry.Sentry;

@WebListener
public class SentryContextListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Sentry.close();
    }
}