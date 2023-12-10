package dmo.fs.server;

import io.helidon.logging.common.HelidonMdc;
import io.helidon.microprofile.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.IOException;
import java.sql.SQLException;

public final class DodexMain {
    private static final Logger LOGGER = LogManager.getLogger(DodexMain.class.getName());
    public static org.eclipse.microprofile.config.Config config = ConfigProvider.getConfig();

    private DodexMain() {
    }
    public static void main(final String[] args) throws IOException, SQLException, InterruptedException {
        Server server = startServer();
        HelidonMdc.set("name", "Nima");
    }

    static Server startServer() {
        LOGGER.info("Starting Server");
        HelidonMdc.set("name", "");
        return Server.create().start();
    }
}
