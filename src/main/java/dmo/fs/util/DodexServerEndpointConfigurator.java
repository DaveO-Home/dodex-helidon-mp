package dmo.fs.util;

import dmo.fs.routes.WebSocketEndpoint;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DodexServerEndpointConfigurator extends ServerEndpointConfig.Configurator {
    protected static final Logger logger = LogManager.getLogger(DodexServerEndpointConfigurator.class.getName());
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {

        // trying to extend life of connection?????
        sec.getUserProperties().put("idle-connection-timeout","PT30M");

        WebSocketEndpoint webSocketEndpoint = CDI.current().select(WebSocketEndpoint.class).get();
        webSocketEndpoint.setRemoteAddress(request.getHeaders().get("Origin").get(0));
        if (request.getHeaders().containsKey("user-agent")) {
            sec.getUserProperties().put("user-agent", request.getHeaders().get("user-agent").get(0)); // lower-case!
        }
//        ClientManager client = ClientManager.createClient();
//        client.getProperties().put(ClientProperties.SHARED_CONTAINER, true);
//        client.getProperties().put(ClientProperties.SHARED_CONTAINER_IDLE_TIMEOUT, 30);
    }
}
