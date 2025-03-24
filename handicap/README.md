
### Kotlin, gRPC Web Application

### Getting Started

#### Client:

* The __html/javascript__ client is generated from the __.../dodex-helidon-mp/src/grpc/client__ directory. 
    * Execute __`npm install`__ to download the javascript dependencies.

    * When changing the client proto configuration, execute `./proto protos/handicap` to generate the `proto3/gRPC` javascript modules. The proto3 configuration is in `./protos/handicap.proto`.
      
      __Note;__ Executing __`./proto protos/handicap`__ before building the client is optional if the proto configuration has not changed.

    * Execute either `npm run esbuild:build` or `npm run webpack:build` to package the javascript development client. The output is located in **src/main/resources/META-INF/resources/**. When making changes to javascript, html or css, simply rerun `npm run esbuild:build`, if __`helidon dev`__ running, the server will rebuild, refresh the browser to view changes. For proto3 changes, rerun `./proto protos/handicap` first.

        __Note;__ **esbuild** is best for development(very fast) and **webpack** is better for production, e.g. `npm run webpack:prod`.

#### Server:

* The server **proto3/gRPC** classes are auto generated from helidon-mp. The **proto3** configuration is in the  __src/main/proto__ directory. __Note:__ The client and server proto3 configurations should be identical.

* From the dodex-helidon-mp directory, execute `helidon dev` or `gradlew run`.

    __Note__; __Hibernate__ can generate the database tables, however the application is set up to auto create tables based on __`DEFAULT_DB`__.

* The next step is to install/startup the __envoy__ proxy server. The javascript client needs a proxy to convert **http/1* to *http/2* etc. Assuming **envoy** is installed <https://www.envoyproxy.io/docs/envoy/latest/start/install>, execute the `start.envoy` script in the **dodex-helidon-mp**` directory. The configuration for the proxy server is in **.../dodex-helidon-mp/handicap/handicap.yaml**.

* If all goes well, dodex-helidon-mp at ports **8060** and **8061(gRPC)** should be started.

* In a browser enter **localhost:8060/handicap.html**.

* The frontend html/gRPC javascript client should display. See operation section on how to use the form.

### Production Build

* In `dodex-helidon-mp/src/grpc/client` execute `npm run webpack:prod`, `esbuild:prod` also works.
* In `dodex-helidon-mp` build with one of these commands __`./gradlew build`__, __`mvn package`__ or __`helidon build`__
* To start the production service with a gradle build, execute `java -jar build/libs/dodex-helidon-mp.jar`
* If `helidon` or `mvn` is used, execute `java -jar target/dodex-helidon-mp.jar` 
* To execute with a particular database and mode(dev/prod);
  * `export DEFAULT_DB=<h2|mariadb|postgres|oracle|mssql|ibmdb2>` and `export MODE=<dev|prod>` before running the jar
  * With Java system properties, execute `java -jar -DDEFAULT_DB=<supported database> -DMODE=<prod|dev> target/dodex-helidon-mp.jar`
  * For the gradle build, execute `java -jar -DDEFAULT_DB=<supported database> -DMODE=<prod|dev> build/libs/dodex-helidon-mp.jar`
* Execute a browser with **url** http://localhost:8060/handicap.html

  __Note;__ Make sure **dodex** is installed before building the jar file. In __src/main/resources/WEB/static__, execute `npm install`.

### Operation

The following are the steps to add golfer info, courses with tee data and golfer scores.

* First time login simply enter a **pin**(2 alpha characters with between 4-6 addition alphanumeric characters) with first and last name. Click the login button to create/login to the application. On subsequent logins only the `pin` is needed, don't forget it. The **Country** and **State** should also be selected before creating a *pin* as default values. However, you can change the defaults on any login. Also, **Overlap Years** and **Public** should be set.

    * Overlap will use the previous year's scores for the handicap if needed.
    * Public will allow your scores to show up in the **Scores** tab for public viewing.
    
* Add a course by entering its name with one radio button selected for the tee. You can also change the tee's color. The **rating**, **slope** and **par** values are also required. Click the **Add Tee** button. After the first added tee, the others can be added when needed.

__Note:__: You can disable the course/tee add function by setting **handicap.enableAdmin** to **true** in the **.../resources/application.yaml** file. And then use the default **`admin.pin`** to administer the courses/tees. When using this pin, a first and last name must be entered on initial use.

* To add a score, select a course and tee with values for **Gross Score**, **Adjusted Score** and **Tee Time**. Click the **Add Score** button to add the score. The **Remove Last Score** will remove the last entered score, multiple clicks will remove multiple scores.

__Note;__ A handicap will be generated after 5 scores have been added.

### Handicap File Structure

```
src/grpc
└── client
    ├── config
    │   ├── esbuild.config.js
    │   ├── esbuild.group.config.js
    │   └── webpack.config.js
    ├── css
    │   ├── app.css
    │   ├── dodex_handicap.css
    │   ├── dtsel
    │   └── styles.css
    ├── grpcdebug_config.yaml
    ├── html
    │   └── index.template.html
    ├── js
    │   ├── client.js
    │   ├── country-states
    │   ├── dodex
    │   ├── dtsel
    │   ├── handicap
    │   ├── validate
    │   └── weather.js
    ├── package.json
    ├── package-lock.json
    ├── pgdump
    ├── postgresql.dmp
    ├── proto
    ├── protos
    │   └── handicap.proto
    └── static
        ├── assets
        ├── content.js
        ├── content.json
        ├── content.private.js
        ├── dodex_g.ico
        ├── favicon.ico
        ├── golf01.jpg
        ├── golf02.jpg
        ├── golf03.jpg
        ├── golf04.jpg
        ├── golf05.jpg
        ├── golf06.jpg
        ├── golf07.jpg
        ├── golf08.jpg
        ├── golf09.jpg
        ├── golf10.jpg
        ├── golf11.jpg
        ├── handicap_info.js
        ├── handicap.proto
        ├── json
        ├── more_horiz.png
        ├── service-worker.js
        └── weather.html

src/main/kotlin
└── golf
    └── handicap
        ├── Course.kt
        ├── Courses.kt
        ├── db
        │   ├── IPopulateCourse.kt
        │   ├── IPopulateGolfer.kt
        │   ├── IPopulateGolferScores.kt
        │   ├── IPopulateScore.kt
        │   ├── PopulateCourse.kt
        │   ├── PopulateGolfer.kt
        │   ├── PopulateGolferScores.kt
        │   ├── PopulateScore.kt
        │   └── SqlConstants.kt
        ├── entities
        │   ├── PersistedCourse.kt
        │   ├── PersistedGolfer.kt
        │   ├── PersistedRatings.kt
        │   └── PersistedScores.kt
        ├── Golfer.kt
        ├── Handicap.kt
        ├── package-info.kt
        └── Score.kt

src/main/java
└── dmo
    └── fs
        ├── db
        │   ├── bld
        │   │   ├── DatabaseBuild.java
        │   │   └── Database.java
        │   ├── db2
        │   │   ├── DbIbmDB2.java
        │   │   └── DodexDatabaseIbmDB2.java
        │   ├── DodexDatabaseBase.java
        │   ├── DodexDatabase.java
        │   ├── fac
        │   │   └── DbConfiguration.java
        │   ├── h2
        │   │   ├── DbH2.java
        │   │   └── DodexDatabaseH2.java
        │   ├── mariadb
        │   │   ├── DbMariadb.java
        │   │   └── DodexDatabaseMariadb.java
        │   ├── mssql
        │   │   ├── DbMssql.java
        │   │   └── DodexDatabaseMssql.java
        │   ├── ora
        │   │   ├── DbOracle.java
        │   │   └── DodexDatabaseOracle.java
        │   ├── postgres
        │   │   ├── DbPostgres.java
        │   │   └── DodexDatabasePostgres.java
        │   └── srv
        │       ├── DodexService.java
        │       ├── GroupService.java
        │       └── package-info.java
        ├── entities
        │   ├── Group.java
        │   ├── Member.java
        │   ├── Message.java
        │   ├── Undelivered.java
        │   └── User.java
        ├── Message.java
        ├── package-info.java
        ├── routes
        │   ├── GroupResource.java
        │   ├── HandicapServiceHealthCheck.java
        │   ├── HandicapService.java
        │   ├── WebSocketEndpointBase.java
        │   └── WebSocketEndpoint.java
        ├── server
        │   └── DodexMain.java
        └── util
            ├── ColorUtilConstants.java
            ├── DodexServerEndpointConfigurator.java
            ├── DodexUtil.java
            ├── Group.java
            ├── MessageUserImpl.java
            └── MessageUser.java

src/main/proto
└── handicap.proto

```