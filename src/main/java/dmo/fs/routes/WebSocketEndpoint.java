package dmo.fs.routes;

import dmo.fs.util.DodexServerEndpointConfigurator;
import dmo.fs.util.DodexUtil;
import io.helidon.security.Security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Locale;

@ServerEndpoint(value = "/dodex", configurator = DodexServerEndpointConfigurator.class)
@ApplicationScoped
public class WebSocketEndpoint extends WebSocketEndpointBase implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    protected static final Logger logger = LogManager.getLogger(WebSocketEndpoint.class.getName());
    @Inject
    private Security security;

    public WebSocketEndpoint() throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$s] %5$s %3$s %n");
        System.setProperty("dmo.fs.level", "INFO");
        System.setProperty("org.jooq.no-logo", "true");
        String value = DodexUtil.getMode();

        Locale.setDefault(Locale.US);
        if (isProduction) {
            DodexUtil.setEnv("prod");
        } else {
            DodexUtil.setEnv(value);
        }
    }

    @Override
    @OnOpen
    public void onOpen(Session session) throws InterruptedException, IOException, SQLException {
        sessions.put(session.getId(), session);
        session.setMaxIdleTimeout(1000 * 60 * 30);

        setup();

        doConnection(session);
        broadcast(session, "User " + session.getRequestParameterMap().get("handle").get(0) + " joined");
    }

    @OnMessage
    public void onMessage(Session session, String message) throws SQLException, IOException, InterruptedException {
        doMessage(session, message);
    }

    @Override
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());

        if (logger.isInfoEnabled()) {
            logger.info("Closing ws-connection to client: {}", session.getRequestParameterMap().get("handle").get(0));
        }
        broadcast(session, "User " + session.getRequestParameterMap().get("handle").get(0) + " left");
        security.createContext("dodex").logout();
    }

    @Override
    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session.getId());
        if (logger.isInfoEnabled()) {
            logger.info("Websocket-failure...User {} {} {}", session.getRequestParameterMap().get("handle").get(0), "left on error:", throwable.getMessage());
        }
        throwable.printStackTrace();
    }
}
