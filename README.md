# dodex-helidon-MP

A synchronous server for Dodex, Dodex-Input and Dodex-Mess using the Helidon Virtual Threaded Java Framework.

## Install Assumptions

1. Java 21+ installed with JAVA_HOME set.
2. Maven and Gradle 8.5.0 installed.
3. Javascript **node** with **npm** package manager installed.
4. Helidon cli client.

### Install Dodex-Helidon-MP

Execute `npm install dodex-helidon-mp` or download from <https://github.com/DaveO-Home/dodex-helidon-mp>. If you use npm install, move node_modules/dodex-helidon to an appropriate directory.

### Why Helidon MP

* Helidon supports two frameworks, __Helidon MP__ and __Helidon SE__. __MP__ is a Java EE annotated microprofile configuration whereas __SE__ is a framework supporting reactive programming.
* Java 21+ and frameworks using Virtual Threads(Helidon MP) may be performant using traditional synchronous programming versus non-blocking asynchronous code on modern computer chip architecture.
* Microprofile compliant

## Getting Started

### Quick Getting Started (docker)

1. cd .../dodex-helidon-mp/src/main/resources/WEB/static and execute __`npm install`__ to install the dodex modules.
2. In .../dodex-helidon-mp Execute __`docker build -t dodex-helidon:latest  .`__
3. Execute __`docker run -d -p 8060:8060 --name dodex_helidon dodex-helidon:latest`__
4. View in browser; __localhost:8060/test/index.html__ or __localhost:8060/test/bootstrap.html__
5. To verify that the image is working, execute __`docker exec -ti  --tty  dodex_helidon /bin/sh`__
6. To keep and run later, execute `docker stop dodex_helidon` and later `docker start dodex_helidon`
7. To clean-up execute `docker stop dodex_helidon` and `docker rm dodex_helidon` and `docker rmi dodex-helidon`
8. To verify cleanup execute `docker imiages`, execute __`docker rmi <image id>`__ to remove unwanted images.  
    __Note:__ Assumes that `dodex-helidon-mp` is set up to use the __"h2"__ database, the default.

### Building `dodex-helidon-mp`

1. cd .../dodex-helidon-mp/src/main/resources/WEB/static and execute __`npm install --save`__ to install the dodex modules.
2. To build for development use the __helidon__ client as it supports live-reload. Execute __`helidon dev`__, uses __pom.xml__.
3. The other build method is; cd .../dodex-helidon-mp and execute __`gradlew run`__. This should also install java dependencies and startup the server against the default __h2__ database.
4. Execute url `http://localhost:8060/test/index.html` in a browser.
5. You can also run `http://localhost:8060/test/bootstrap.html` for a bootstrap example.
6. Follow instructions for dodex at <https://www.npmjs.com/package/dodex-mess> and <https://www.npmjs.com/package/dodex-input>.

### Building `gRPC Application`

1. cd .../dodex-helidon-mp/src/grpc/client and execute __`npm install`__ to install client dependencies.
2. execute __`npm run esbuild:build`__ or __`npm run esbuild:prod`__ to build the gRPC client. Using __`esbuild`__ is fast and works well in development.
3. Alternatively, execute __`npm run webpack:build`__ or __`npm run webpack:prod`__ for usage in a production __jar__.
4. The __`gRPC`__ application uses a javascript client and requires a Proxy Server to communicate with the backend Helidon server.
5. Included is a __`Envoy`__ configuration in `.../dodex-helidon-map/handicap/handicap.yaml`
6. To use the envoy proxy;
   * Install the envoy executable or startup as a docker container
   * Once installed, startup by executing __`.../dodex-helidon-mp/start.envoy`__
7. Startup the Helidon Server; __`helidon dev`__ or `gradlew run`
8. Browse the application at __`localhost:8060/handicap.html`__
9. Additional information; https://github.com/DaveO-Home/dodex-helidon-mp/blob/main/handicap/README.md

### Operation

1. The Dodex-Helidon backend uses __Hibernate/HikariCP__ to persist data.
2. The application is configured to use __Websocket__, __OpenAPI__ and __gRPC__ endpoints to communicate with frontend HTML/javascript.
3. Multiple databases are supported, to configure see;  
   * __.../src/main/resources/META-INF/persistence.xml__ for connection properties, persistence units and hibernate configuration.
4. Once the connection properties are configured, simply set the environment variable __`DEFAULT_DB`__ to one of the databases listed below. The mode defaults to __`dev`__, set __`MODE=prod`__ for production.
   *  __`h2`__
   *  __`postgres`__
   *  __`mariadb`__
   *  __`oracle`__
   *  __`mssql`__
   *  __`ibmdb2`__
5. Building the Production Jar with supporting libraries using Maven.
   * Before building the Jar for production.
      * Make sure dodex is installed at ./src/main/resources/WEB/static by running __`npm install`__.
      * Also consider setting up a production database.
   * Execute __`mvn package`__ to generate the production jar(dodex-helidon-mp.jar) in __./target__ and __./target/libs__.
   * Set desired environment variables __`DEFAULT_DB`__ and __`MODE`__ or use Java system properties __`-DDEFAULT_DB=postgres`__ and __`-DMODE=prod`__.
   * Execute __`java -jar target/dodex-helidon-mp.jar`__ to start up the production server. 
6. Building the Production Jar with supporting libraries using Gradle.
   * Execute __`./gradlew clean build`__ to generate the production jar(dodex-helidon-mp.jar) in __./build/libs__`.
   * Set desired environment variables __`DEFAULT_DB`__ and __`MODE`__ or use Java system properties __`-DDEFAULT_DB=postgres`__ and __`-DMODE=prod`__.
   * Execute __`java -jar build/libs/dodex-helidon-mp.jar`__ to start up the production server.
7. Execute url `http://localhost:8060/test/index.html` or `.../test/bootstrap.html` in a browser.
8. Execute url `http://localhost:8060/handicap.html` for the `gRPC` application.

## Java Linting with PMD

__Note:__ PMD is not ready for Java-21 - "ClassNotFoundException: net.sourceforge.pmd.ant.PMDTask". However, all the PMD 7 deprecations have been fixed in __`dodexstart.xml`__.
* Run `gradlew pmdMain` and `gradlew pmdTest` to verify code using a subset of PMD rules in __`dodexstart.xml`__
* Reports can be found in `build/reports/pmd`

## Test Dodex

1. Make sure the demo **dodex-helidon-mp** server is running in development mode.
2. Test Dodex-mess by entering the URL **localhost:8060/test/index.html** in a browser.
3. Ctrl+Double-Click a dial or bottom card to pop up the messaging client.
4. To test the messaging, open up the URL in a different browser and make a connection by Ctrl+Double-Clicking the bottom card. Make sure you create a handle.
5. Enter a message and click **send** to test.
6. For dodex-input Double-Click a dial or bottom card to pop up the input dialog. Allows for uploading, editing and removal of private content. Content in JSON can be defined as arrays to make HTML more readable.
7. Standalone Dodex can be used as a normal rolodex as well as a menu launcher(localhost:8060/test/bootstrap.html).  

## Dodex Groups using OpenAPI

* A default javascript client is included in __.../dodex-helidon-mp/src/main/resources/WEB/static/group/__. It can be regenerated in __.../dodex-helidon-mp/src/openapi/client/__ by executing __`npm run group:prod`__.
* The group javascript client is in __.../src/grpc/client/js/dodex/groups.js__ and __group.js__.  
  __Note:__ The client is included in the application by default.
* See __.../src/main/resources/META-INF/openapi.yaml__ for OpenAPI declarations. You can view and test the configuration for development at __http://localhost:8060/openapi/ui/index.html__. If there is an error, explore with __/openapi/__.
* The implementation uses a __REST__ api in the __GroupResource__ class.

### Installing in Dodex (already included in demo)
1. Implementing in a javascript module; see __.../dodex-helidon-mp/handicap/src/grpc/client/js/dodex/index.js__  
   * `import { groupListener } from "./groups";`
   * in the dodex init configuration, add  
      ```
     ...
      .then(function () {
           groupListener();
     ...
      ```
2. Implementing with inline html; see __.../dodex-helidon-mp/main/resources/WEB/static/test/index.html__  
   * `<script src="../group/main.min.js"></script>`
   * in the dodex init configuration, add  
      ```
     ...
      .then(function () {
           window.groupListener();
     ...
      ```  
3. Using dodex-messaging group functionality  
   __Note:__ Grouping is only used to limit the list of "handles" when sending private messages.
   * Adding a group using `@group+<name>`
     * select __Private Message__ from the __more__ button dropdown to get the list of handles.
     * enter `@group+<name>` for example `@group+aces`
     * select the handles to include and click "Send". Members can be added at any subsequent time.
   * Removing a group using `@group-<name>`
     * enter `@group-<name>` for example `@group-aces` and click "Send". Click the confirmation popup to complete.
   * Removing a member
     * enter `@group-<name>` for example `@group-aces`
     * select a "handle" from the dropdown list and click "Send"
   * Selecting a group using `@group=<name>`
     * enter `@group=<name>` for example `@group=aces` and click "Send"
     * Select from reduced set of "handles" to send private message.

By default, the entry `"dodex.groups.checkForOwner"` in __microprofile-config.properties__ is set to __false__. This means that any "handle" can delete a "group" or "member". Setting the entry to __true__ prevents global administration, however, if the owner "handle" changes, group administration is lost.  
        
The grouping function also implements __Basic HTTP Security__, see __application.yaml__. The basic security is enabled by setting __security.jersey.enabled__ to true. A user logging in with the __admin__ role can manipulate any group.  

A __Logout__ button on the demo dodex pages .../test/index.html and .../test/bootstrap.html is provided to change the basic login. It may require a `cntl alt f5` and browser restart to work.


## ChangeLog

<https://github.com/DaveO-Home/dodex-helidon-mp/blob/master/CHANGELOG.md>

## Authors

* *Initial work* - [DaveO-Home](https://github.com/DaveO-Home)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
