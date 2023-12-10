package dmo.fs.routes;

import dmo.fs.db.DodexDatabase;
import dmo.fs.db.fac.DbConfiguration;
import dmo.fs.util.DodexUtil;
import dmo.fs.util.MessageUser;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class WebSocketEndpointBase {
  protected static final Logger logger = LogManager.getLogger(WebSocketEndpointBase.class.getName());
  protected final boolean isProduction = DodexUtil.getMode().equals("prod");
  protected boolean isReactive;
  protected boolean isSetupDone;
  protected boolean isInitialized;
  protected DodexDatabase dodexDatabase;

  protected Map<String, Session> sessions = new ConcurrentHashMap<>();

  public void setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  protected String remoteAddress;

  protected WebSocketEndpointBase() throws IOException {
  }

  @OnOpen
  public abstract void onOpen(Session session) throws InterruptedException, IOException, SQLException;

  @OnClose
  public abstract void onClose(Session session);

  @OnError
  public abstract void onError(Session session, Throwable throwable);

  protected void broadcast(Session session, String message) {
    sessions.values().stream().filter(s -> !s.getId().equals(session.getId())).forEach(s -> {
      if (s.isOpen()) {
        s.getAsyncRemote().sendObject(message, result -> {
          if (result.getException() != null) {
            logger.warn("Unable to send message: {}{} {}",
                s.getRequestParameterMap().get("handle").get(0), ":", result.getException().getMessage());
          }
        });
      }
    });
  }

  protected void doConnection(Session session) {
    final MessageUser messageUser = setMessageUser(session);

    if (!isSetupDone) {
      isSetupDone = true;
    }
    try {
      MessageUser resultUser = dodexDatabase.selectUser(messageUser, session);

      try {
        String userJson = dodexDatabase.buildUsersJson(resultUser);
        /*
         * Send list of registered users with connected notification
         */
        session.getAsyncRemote().sendObject("connected:" + userJson); // Users for private messages
        /*
         * Send undelivered messages and remove user related messages.
         */
        Map<String, Integer> map = dodexDatabase.processUserMessages(session, resultUser);

        int messageCount = map.get("messages");
        if (messageCount > 0) {
          logger.info("Messages Delivered: {} to {}", messageCount, resultUser.getName());
        }
      } catch (InterruptedException | SQLException e) {
        session.getAsyncRemote().sendText(e.getMessage());
        session.close();
        e.printStackTrace();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void doMessage(Session session, String message) {
    final MessageUser messageUser = setMessageUser(session);
    final ArrayList<String> onlineUsers = new ArrayList<>();
    // Checking if message or command
    final Map<String, String> returnObject = new DodexUtil().commandMessage(message);
    final String selectedUsers = returnObject.get("selectedUsers");
    // message with command stripped out
    final String computedMessage = returnObject.get("message");
    final String command = returnObject.get("command");

    if (";removeuser".equals(command)) {
      try {
        dodexDatabase.deleteUser(session, messageUser);
      } catch (InterruptedException | SQLException e) {
        e.printStackTrace();
        session.getAsyncRemote().sendObject("Your Previous handle did not delete: " + e.getMessage());
      }
    }
    if (!computedMessage.isEmpty()) {
      sessions.values().stream().filter(s -> !s.getId().equals(session.getId()) && session.isOpen())
          .forEach(s -> {
            final String handle = s.getRequestParameterMap().get("handle").get(0);
            // broadcast
            if ("".equals(selectedUsers) && "".equals(command)) {
              s.getAsyncRemote().sendObject(messageUser.getName() + ": " + computedMessage);

            // private message
            } else if (Arrays.stream(selectedUsers.split(",")).anyMatch(h -> {
              boolean isMatched = false;
              isMatched = h.contains(handle);
              return isMatched;
            })) {
              s.getAsyncRemote().sendObject(messageUser.getName() + ": " + computedMessage, result -> {
                if (result.getException() != null && logger.isInfoEnabled()) {
                  logger.info("Websocket-connection...Unable to send message: {}{} {}",
                      s.getRequestParameterMap().get("handle").get(0), ":", result.getException().toString());
                }
              });
              // keep track of delivered messages
              onlineUsers.add(handle);
            }
          });

      if ("".equals(selectedUsers) && !"".equals(command)) {
        session.getAsyncRemote().sendObject("Private user not selected");
      } else {
        session.getAsyncRemote().sendObject("ok");
      }
    }

    // calculate difference between selected and online users
    if (!"".equals(selectedUsers)) {
      final List<String> selected = Arrays.asList(selectedUsers.split(","));
      final List<String> disconnectedUsers = selected.stream().filter(user -> !onlineUsers.contains(user))
          .collect(Collectors.toList());
      // Save undelivered message to send when to-user logs in
      if (!disconnectedUsers.isEmpty()) {
        try {
          Long id = dodexDatabase.addMessage(session, messageUser, computedMessage);

          try {
            dodexDatabase.addUndelivered(session, messageUser, disconnectedUsers, id);
          } catch (SQLException e) {
            e.printStackTrace();
          }
        } catch (InterruptedException | SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected MessageUser setMessageUser(Session session) {
    final MessageUser messageUser = dodexDatabase.createMessageUser();
    Session sess = sessions.get(session.getId());
    if (sess == null) {
      sess = session;
    }
    String handle;
    String id;

    handle = sess.getRequestParameterMap().get("handle").get(0);
    id = sess.getRequestParameterMap().get("id").get(0);

    messageUser.setName(handle);
    messageUser.setPassword(id);
    messageUser.setIp(session.getBasicRemote() == null ? "Unknown" : remoteAddress);

    return messageUser;
  }

  protected void setup() throws InterruptedException, IOException, SQLException {
    dodexDatabase = DbConfiguration.getDefaultDb();
    dodexDatabase.entityManagerSetup();
    dodexDatabase.databaseSetup();
    dodexDatabase.configDatabase();

    /*
     * Optional auto user cleanup - config in "application-conf.json". When client
     * changes handle when server is down, old users and undelivered messages will
     * be orphaned.
     *
     * Defaults: off - when turned on 1. execute on start up and every 7 days
     * thereafter. 2. remove users who have not logged in for 90 days.
     */

    /*
    @todo convert to hibernate
     */

//        final Optional<Context> context = Optional.ofNullable(vertx.getOrCreateContext());
//        if (context.isPresent()) {
//            final Optional<JsonObject> jsonObject = Optional.ofNullable(vertx.getOrCreateContext().config());
//            try {
//                JsonObject config = jsonObject.orElseGet(JsonObject::new);
//                if (config.isEmpty()) {
//                    ObjectMapper jsonMapper = new ObjectMapper();
//                    JsonNode node;
//
//                    try (InputStream in = getClass().getResourceAsStream("/application-conf.json")) {
//                        node = jsonMapper.readTree(in);
//                    }
//                    config = JsonObject.mapFrom(node);
//                }
//                final Optional<Boolean> runClean = Optional.ofNullable(config.getBoolean("clean.run"));
//                if (runClean.isPresent() && runClean.get().equals(true)) {
//                    final CleanOrphanedUsers clean = new CleanOrphanedUsers();
//                    clean.setDatabase(dodexDatabase);
//                    clean.setPromise(cleanupPromise);
//                    clean.startClean(config);
//                }
//            } catch (final Exception exception) {
//                logger.info(String.format("%sContext Configuration failed...%s%s", ColorUtilConstants.RED_BOLD_BRIGHT,
//                        exception.getMessage(), ColorUtilConstants.RESET));
//                exception.printStackTrace();
//            }
//        }

    String db = DbConfiguration.getDb();
    String startupMessage = "In Production with database: " + db;

    startupMessage = "dev".equals(DodexUtil.getEnv()) ? "In Development with database: " + db
        : startupMessage;
    logger.info("Starting Web Socket...{} -- {}", startupMessage, DodexUtil.getEnv());
  }
}
