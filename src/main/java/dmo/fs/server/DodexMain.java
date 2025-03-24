package dmo.fs.server;

import dmo.fs.routes.HandicapService;
import dmo.fs.routes.HandicapServiceHealthCheck;
import dmo.fs.util.ColorUtilConstants;
import dmo.fs.util.DodexUtil;
import io.helidon.config.Config;
import io.helidon.logging.common.HelidonMdc;
import io.helidon.microprofile.server.Server;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.grpc.GrpcRouting;
import io.helidon.webserver.observe.ObserveFeature;
import io.helidon.webserver.observe.health.HealthObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.ConfigProvider;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public final class DodexMain {
    private static final Logger LOGGER = LogManager.getLogger(DodexMain.class.getName());
    public static org.eclipse.microprofile.config.Config config = ConfigProvider.getConfig();

    private DodexMain() {
    }

    public static void main(final String[] args) throws IOException, SQLException, InterruptedException {
        Config config = Config.empty();

        Server server = startServer();
        WebServer webServer = startServer(config);
        HelidonMdc.set("name", "Nima");
        checkInstallation();
    }
    /* gRPC Server */
    static WebServer startServer(Config serverConfig) throws SQLException, IOException, InterruptedException {
        ObserveFeature observe = ObserveFeature.builder()
          .addObserver(HealthObserver.builder()
//            .config(serverConfig.get("server.features.observe")) // .get("features.observe.observers.health"))
            .addCheck(new HandicapServiceHealthCheck(serverConfig))
            .build())
          .build();

        return WebServer.builder()
            .host("0.0.0.0")
            .port(8061)
            .addRouting(GrpcRouting.builder().service(new HandicapService()))
            .addFeature(observe)
            .build()
            .start();
    }

    static Server startServer() {
        LOGGER.info("Starting Http Server");

        return Server.builder().build().start();
    }

    static void checkInstallation() {
        String fileDir = "./src/main/resources/WEB/static/node_modules/";
        File checkDir = new File(fileDir);
        String mode = DodexUtil.getMode();
        if("dev".equals(mode)) {
            if (!checkDir.exists()) {
                LOGGER.warn("{}{}{}", ColorUtilConstants.CYAN_BOLD_BRIGHT,
                  "To install Dodex, execute 'npm install' in 'src/main/resources/WEB/static/'"
                  , ColorUtilConstants.RESET);
            }
        }
        fileDir = "./src/grpc/client/node_modules/";
        checkDir = new File(fileDir);
        mode = DodexUtil.getMode();
        if("dev".equals(mode)) {
            if (!checkDir.exists()) {
                LOGGER.warn("{}{}{}", ColorUtilConstants.CYAN_BOLD_BRIGHT,
                  "To install gRPC, execute 'npm install' in 'src/grpc/client/' and 'npm run esbuild:prod' or 'npm run webpack:prod'"
                  , ColorUtilConstants.RESET);
            }
        }
    }
}
