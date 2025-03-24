package dmo.fs.routes;

import com.google.protobuf.Descriptors;
import dmo.fs.db.DodexDatabaseBase;
import dmo.fs.db.fac.DbConfiguration;
import golf.handicap.Golfer;
import golf.handicap.Handicap;
import golf.handicap.Score;
import golf.handicap.db.PopulateCourse;
import golf.handicap.db.PopulateGolfer;
import golf.handicap.db.PopulateGolferScores;
import golf.handicap.db.PopulateScore;
import handicap.grpc.*;
import io.grpc.stub.StreamObserver;
import io.helidon.grpc.core.ResponseHelper;
import io.helidon.webserver.grpc.GrpcService;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonPointer;
import jakarta.json.bind.JsonbBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static dmo.fs.server.DodexMain.config;

public class HandicapService implements GrpcService {
    private static final Logger logger = LogManager.getLogger(HandicapService.class.getName());
    DodexDatabaseBase dodexDatabase;
    EntityManagerFactory emf;
    EntityManager entityManager;
    String enableHandicapAdmin = "false";
    String handicapAdminPin = null;

    public HandicapService() {
        try {
            dodexDatabase = DbConfiguration.getDefaultDb();
            dodexDatabase.entityManagerSetup();
            dodexDatabase.databaseSetup();
            dodexDatabase.configDatabase();
            emf = dodexDatabase.getEmf();
            entityManager = emf.createEntityManager();

            Map<String,String> yamlMap = new HashMap<>();
            for (ConfigSource configSource : config.getConfigSources()) {
                if(configSource.getName().startsWith("yaml:")) {
                    yamlMap = configSource.getProperties();
                }
            }

            Optional<String> enableAdmin = Optional.ofNullable(yamlMap.get("handicap.enable.admin"));
            enableAdmin.ifPresent(admin -> enableHandicapAdmin = admin);
            Optional<String> handicapPin = Optional.ofNullable(yamlMap.get("handicap.admin.pin"));
            handicapPin.ifPresent(pin -> handicapAdminPin = pin);
        } catch (SQLException | IOException | InterruptedException ex) {
            logger.info(new Exception("Persistence Unit Failed: " + ex.getMessage()).getMessage());
            ex.printStackTrace();
        }
    }

    public void getGolfer(HandicapSetup request, StreamObserver<HandicapData> observer) {
        JsonObject requestJson = Json.createObjectBuilder().add("data", request.getJson()).build();
        Golfer handicapGolfer = JsonbBuilder.create().fromJson(requestJson.getString("data"), Golfer.class);
        JsonObject responseObject = Json.createObjectBuilder().build();
        try {
            Golfer currentGolfer = new PopulateGolfer().getGolfer(handicapGolfer, request.getCmd(), entityManager);
            String json = JsonbBuilder.create().toJson(currentGolfer);

            responseObject = Json.createReader(new StringReader(json)).readObject();
            if ("true".equals(enableHandicapAdmin)) {
                responseObject = Json.createObjectBuilder(responseObject)
                  .add("admin", handicapAdminPin)
                  .add("adminstatus", 10)
                  .build();
            }
        } catch (SQLException | InterruptedException ex) {
            ex.printStackTrace();
        }

        HandicapData handicapData = HandicapData.newBuilder()
          .setMessage("success")
          .setCmd(request.getCmd())
          .setJson(responseObject.toString())
          .build();

        ResponseHelper.complete(observer, handicapData);
    }

    public void addRating(Command request, StreamObserver<HandicapData> observer) {
        String json = request.getJson();
        PopulateCourse populateCourse = new PopulateCourse();
        HashMap<String, Object> ratingMap = populateCourse.getRatingMap(json);
        HandicapData handicapData;
        handicapData = populateCourse.getCourseWithTee(ratingMap, entityManager);

        ResponseHelper.complete(observer, handicapData);
    }

    public void getCourses(
      Command request,
      StreamObserver<HandicapData> responseObserver
    ) {
        PopulateCourse populateCourse = new PopulateCourse();

        String coursesJson = populateCourse.getCourses(request.getKey(), entityManager);

        HandicapData handicapData = HandicapData.newBuilder()
          .setMessage("success")
          .setCmd(request.getCmd())
          .setJson(coursesJson)
          .build();

        ResponseHelper.complete(responseObserver, handicapData);
    }

    public void getGolfers(
      Command request,
      StreamObserver<HandicapData> responseObserver) {

        PopulateGolfer populateGolfer = new PopulateGolfer();

        String golfersJson;
        try {
            golfersJson = populateGolfer.getGolfers(request, entityManager);
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        HandicapData handicapData = HandicapData.newBuilder()
          .setMessage("Success")
          .setCmd(request.getCmd())
          .setJson(golfersJson)
          .build();
        ResponseHelper.complete(responseObserver, handicapData);
    }

    public void addScore(Command request, StreamObserver<HandicapData> responseObserver) {
        JsonObject requestJson = Json.createObjectBuilder().add("data", request.getJson()).build();
        Score golferScore = JsonbBuilder.create().fromJson(requestJson.getString("data"), Score.class);
        PopulateScore populateScore = new PopulateScore();

        String scoreJson;

        Golfer golfer = Objects.requireNonNull(golferScore.getGolfer());

        Handicap handicap = new Handicap();
        handicap.setScore(golferScore);
        Map<String, Object> latestTee = handicap.getHandicap(golfer, entityManager);

        float newHandicap = (Float) latestTee.get("handicap");
        float slope = latestTee.get("slope") == null ? 0f : (Float) latestTee.get("slope");
        float rating = latestTee.get("rating") == null ? 0f : (Float) latestTee.get("rating");
        int par = latestTee.get("par") == null ? 0 :(Integer) latestTee.get("par");
        golferScore.setHandicap(newHandicap);
        float courseHandicap = newHandicap * slope / 113 + (rating - par);

        golferScore.setNetScore(golferScore.getGrossScore() - courseHandicap);
        golferScore.getGolfer().setHandicap(newHandicap);

        scoreJson = populateScore.setScore(golferScore, entityManager);

        HandicapData handicapData = HandicapData.newBuilder()
          .setMessage("Success")
          .setCmd(request.getCmd())
          .setJson(scoreJson)
          .build();
        ResponseHelper.complete(responseObserver, handicapData);
    }

    public void golferScores(Command request, StreamObserver<HandicapData> responseObserver) {
        PopulateGolferScores populateScores = new PopulateGolferScores();
        JsonObject requestJson = Json.createObjectBuilder().add("data", request.getJson()).build();
        Golfer golfer = JsonbBuilder.create().fromJson(requestJson.getString("data"), Golfer.class);

        if (request.getCmd() == 10) {
            String[] names = request.getKey().split(" ");

            golfer.setFirstName(names[0]);
            golfer.setLastName(names.length > 1 ? names[1].trim() : "");
            golfer.setPin("");
        }

        Map<String, Object> scoresMap = populateScores.getGolferScores(golfer, 365, entityManager);
        responseObserver.onNext(
          HandicapData.newBuilder()
            .setMessage("Success")
            .setCmd(request.getCmd())
            .setJson(scoresMap.get("array").toString())
            .build()
        );
        responseObserver.onCompleted();
    }

    public void removeScore(Command request, StreamObserver<HandicapData> responseObserver) {
        PopulateGolferScores populateGolferScores = new PopulateGolferScores();
        JsonObject requestJson = Json.createObjectBuilder().add("data", request.getJson()).build();

        Golfer golfer = JsonbBuilder.create().fromJson(requestJson.getString("data"), Golfer.class);
        if (golfer.getPin() != null) {
            String used = populateGolferScores.removeLastScore(request.getKey(), entityManager);

            Handicap handicap = new Handicap();
            Map<String, Object> latestTee = handicap.getHandicap(golfer, entityManager);
            double handicapValue = Double.parseDouble(latestTee.get("handicap").toString());
            golfer.setHandicap(handicapValue);
            golfer.setStatus(Integer.parseInt(used));

            String golferJson = JsonbBuilder.create().toJson(golfer, Golfer.class);

            HandicapData handicapData = HandicapData.newBuilder()
              .setMessage("Success")
              .setCmd(request.getCmd())
              .setJson(golferJson)
              .build();

            ResponseHelper.complete(responseObserver, handicapData);

        }
    }

    public void listCourses(
      Command request,
      StreamObserver<ListCoursesResponse> responseObserver
    ) {
        PopulateCourse populateCourse = new PopulateCourse();
        JsonObject requestJson = Json.createObjectBuilder().add("data", request.getJson()).build();

        ListCoursesResponse.Builder coursesBuilder = populateCourse.listCourses(request.getKey(), entityManager);

        ResponseHelper.complete(responseObserver, coursesBuilder.build());
    }

    @Override
    public Descriptors.FileDescriptor proto() {
        return HandicapProto.getDescriptor();
    }

    @Override
    public void update(Routing routing) {
        routing.unary(
          "GetGolfer", this::getGolfer
        );
        routing.unary(
          "AddRating", this::addRating
        );
        routing.unary(
          "GetCourses", this::getCourses
        );
        routing.unary(
          "GetGolfers", this::getGolfers
        );
        routing.unary(
          "AddScore", this::addScore
        );
        routing.unary(
          "GolferScores", this::golferScores
        );
        routing.unary(
          "RemoveScore", this::removeScore
        );
        routing.unary(
          "ListCourses", this::listCourses
        );
    }
}
