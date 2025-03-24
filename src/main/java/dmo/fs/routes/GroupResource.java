package dmo.fs.routes;

import dmo.fs.db.srv.GroupService;
import dmo.fs.util.Group;
import io.helidon.security.annotations.Authenticated;
import io.helidon.security.annotations.Authorized;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Map;

@Path("groups")
@RequestScoped
@OpenAPIDefinition(info = @Info(title = "Dodex Group API", version = "3.0.3"))
public class GroupResource implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    protected static final Logger logger = LogManager.getLogger(GroupResource.class.getName());
    protected static boolean isDebug = System.getenv("DEBUG") != null || System.getProperty("DEBUG") != null;

    /*
        Not used, included for OpenApi-UI
     */
    @GET
    @Path("/groups")
    @Authenticated
    @Authorized
    @RolesAllowed({"admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group openApiGetGroups() {
        return new Group();
    }

    @PUT
    @Path("/addGroup")
    @Authenticated
    @Authorized
    @RolesAllowed({"admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group openApiAddGroup(Group group) throws IOException {
        JsonBuilderFactory groupJson = Json.createBuilderFactory(Map.of());
        JsonObject jsonObject = groupJson.createObjectBuilder(group.getMap()).build();
        GroupService groupResource = CDI.current().select(GroupService.class).get();

        jsonObject = groupResource.addGroupAndMembers(jsonObject);

        group = JsonbBuilder.create().fromJson(jsonObject.toString(), Group.class);

        return group;
    }

    @DELETE
    @Path("removeGroup")
    @Authenticated
    @Authorized
    @RolesAllowed({"admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group openApiDeleteGroup(Group requestGroup) throws SQLException, IOException, InterruptedException {
        JsonBuilderFactory groupJson = Json.createBuilderFactory(Map.of());
        JsonObject jsonObject = groupJson.createObjectBuilder(requestGroup.getMap()).build();

        GroupService groupResource = CDI.current().select(GroupService.class).get();

        jsonObject = groupResource.deleteGroupOrMembers(jsonObject);

        requestGroup = JsonbBuilder.create().fromJson(jsonObject.toString(), Group.class);

        return requestGroup;
    }

    @POST
    @Path("/getGroup/{groupId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group openApiById(String json) throws SQLException, IOException, InterruptedException {
        Group group = JsonbBuilder.create().fromJson(json, Group.class);
        logger.debug("Group Post Request: {}", group.getMap());

        JsonBuilderFactory groupJson = Json.createBuilderFactory(Map.of());

        JsonObject jsonObject = groupJson.createObjectBuilder(group.getMap()).build();
        GroupService groupResource = CDI.current().select(GroupService.class).get();

        jsonObject = groupResource.getMembersList(jsonObject);

        Group responseGroup = JsonbBuilder.create().fromJson(jsonObject.toString(), Group.class);

        logger.debug("Group Post Response: {} -- {}", group.getMap(), jsonObject.toString());
        return responseGroup;
    }
}
