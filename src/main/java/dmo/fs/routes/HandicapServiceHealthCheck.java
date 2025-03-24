package dmo.fs.routes;

import handicap.grpc.HandicapData;
import handicap.grpc.HandicapServiceGrpc;
import handicap.grpc.HandicapSetup;
import io.helidon.common.tls.Tls;
import io.helidon.config.Config;
import io.helidon.health.HealthCheck;
import io.helidon.health.HealthCheckResponse;
import io.helidon.health.HealthCheckType;
import io.helidon.scheduling.Scheduling;
import io.helidon.webclient.api.WebClient;
import io.helidon.webclient.grpc.GrpcClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HandicapServiceHealthCheck implements HealthCheck {
    private static final Logger logger = LogManager.getLogger(HandicapServiceHealthCheck.class.getName());
    private final WebClient webClient;
    private CountDownLatch latch;
    private volatile boolean readiness = true;

    public HandicapServiceHealthCheck(Config config) {
        // set up client to access gRPC service
        Tls clientTls = Tls.builder().enabled(false).build();
//        Tls clientTls = Tls.builder()
//          .trust(trust -> trust
//            .keystore(store -> store
//              .passphrase("password")
//              .trustStore(false)
////              .keystore(Resource.create("client.p12")
//              )).build();
//          .build();
//        int serverPort = config.get("port").asInt().get();
        this.webClient = WebClient.builder()
          .tls(clientTls)
          .baseUri("http://localhost:" + 8061) // serverPort)
          .build();
        logger.info("Starting Health Check!");
    }

    @Override
    public String name() {
        return HandicapService.class.getSimpleName();
    }

    @Override
    public HealthCheckType type() {
        return HealthCheckType.READINESS;
    }

    @Override
    public HealthCheckResponse call() {
        if (latch == null) {
            latch = new CountDownLatch(1);
            Scheduling.fixedRate()          // task to check for readiness
              .delay(1)
              .initialDelay(0)
              .timeUnit(TimeUnit.MINUTES)
              .task(i -> checkReadiness())
              .build();
        }
        try {
            boolean check = latch.await(5, TimeUnit.SECONDS);
            return HealthCheckResponse.builder()
              .status(check && readiness)
              .get();
        } catch (Exception e) {
            logger.info("Response status: {}", e.getMessage());
            return HealthCheckResponse.builder()
              .status(false)
              .get();
        }
    }

    /**
     * Self-invocation to verify gRPC endpoint is available and ready.
     */
    private void checkReadiness() {
        try {
            GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);

            HandicapServiceGrpc.HandicapServiceBlockingStub service =
              HandicapServiceGrpc.newBlockingStub(grpcClient.channel());

            HandicapData res = service.getGolfer(
              HandicapSetup.newBuilder().setMessage("Get Golfer").setCmd(3).setJson("{\"pin\":\"XX1234\"}").build());
            readiness = res.getMessage().equals("No Golfer Found");
//            readiness = true;
        } catch (Exception e) {
            readiness = false;
            logger.info("CheckReadiness: {}", e.getMessage());
        } finally {
            latch.countDown();
        }
    }
}
