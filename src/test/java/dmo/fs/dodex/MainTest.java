/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dmo.fs.dodex;

import dmo.fs.util.Group;
import io.helidon.microprofile.server.Server;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@HelidonTest()
class MainTest {

    private WebTarget target;

    private static Client client;

    String groupMessage = "";
    String name = null;
    String groupName = "aces";
    String groupOwner = "tester";
    String ownerId = "tester";
    Integer status = 0;
    Integer id = 0;
    String created = null;
    String updated = null;
    String errorMessage = "";
    String members = null;
    Integer ownerKey = 0;

    @Inject
    MainTest(WebTarget target) {
        this.target = target;
    }

    @BeforeAll
    public static void startTheServer() {
        client = ClientBuilder.newClient();
    }

    @AfterAll
    static void destroyClass() {
        CDI<Object> current = CDI.current();
        ((SeContainer) current).close();
    }

    @Test
    void testOpenApiRequest() {
        Group group = new Group(groupMessage,
            name,
            groupName,
            groupOwner,
            ownerId,
            status,
            id,
            created,
            updated,
            errorMessage,
            members,
            ownerKey);

        Response response = target.path("groups/getGroup/aces").request().post(Entity.json(group));
        Group groupReq = response.readEntity(Group.class);
        JsonBuilderFactory groupJson = Json.createBuilderFactory(Map.of());
        JsonObject jsonObject = groupJson.createObjectBuilder(groupReq.getMap()).build();
        assertThat("OpenApi Request Query Group", jsonObject.getString("groupName"), is("aces"));
    }
    @Test
    void testLoadStatic() {
        String data = target.path("/test/index.html")
                .request().get(String.class);
        assertThat("default message", data, startsWith("<!DOCTYPE html>"));
    }

    /* test connection to an Oracle Container database (docker) */
    @Test
    @Disabled()
    void testOracleConn() {
        final String DB_URL = "jdbc:oracle:thin:@//192.168.49.1:1521/FREEPDB1";
        final String USER = "dodex";
        final String PASS = "dodex";

        Connection conn = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            assertThat("Oracle Connection", conn.isClosed(), is(false));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }
    @Test
    void testServerUp() {
        Object jdkVersion = client.getConfiguration().getProperty("java.specification.version");
        assertThat("Client has property", Integer.parseInt(jdkVersion.toString()), Matchers.is(greaterThan(20)));
        assertThat("Server has uri", target.getUri().toString(), startsWith("http://localhost"));
    }
}
