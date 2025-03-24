package dmo.fs.db.mariadb;

import dmo.fs.db.DodexDatabaseBase;
import dmo.fs.util.DodexUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.Serial;
import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@ApplicationScoped
public class DodexDatabaseMariadb extends DodexDatabaseBase implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  private final static Logger logger = LogManager.getLogger(DodexDatabaseMariadb.class.getName());

  private static EntityManagerFactory emf;

  public DodexDatabaseMariadb() {
    super();
  }

  @Override
  public void entityManagerSetup() {
    if(emf == null) {
      emf = Persistence.createEntityManagerFactory("mariadb." + DodexUtil.getMode());
    }

    entityManager = emf.createEntityManager();

    Session session = entityManager.unwrap(Session.class);
    SessionFactory sessionFactory = session.getSessionFactory();
    Map<String, Object> properties = sessionFactory.getProperties();
    Object sessionDbname = properties.get("dbname");
    dbName = sessionDbname != null ? sessionDbname.toString() : "test";
    Object mode = properties.get("mode");
  }

  @Override
  public void entityManagerSetup(EntityManagerFactory emf) {
  }

  @Override
  public void configDatabase() {
    String[] types = {"TABLE"};
    /*
      dbName must be consistent with the datasource defined in microprofile-config.properties
     */
    Session session = entityManager.unwrap(Session.class);
    session.doWork(connection -> {
      try {
        connection.setAutoCommit(false);
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        boolean didCreateATable = false;
        ResultSet rsDbname = databaseMetaData.getTables(dbName, null, "users", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("USERS"));
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("USERSSEQ"));
          didCreateATable = true;
          logger.info("Users Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "messages", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("MESSAGES"));
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("MESSAGESSEQ"));
          didCreateATable = true;
          logger.info("Messages Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "undelivered", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("UNDELIVERED"));
          didCreateATable = true;
          logger.info("Undelivered Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "groups", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("GROUPS"));
          didCreateATable = true;
          logger.info("Groups Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "member", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("MEMBER"));
          didCreateATable = true;
          logger.info("Member Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "golfer", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("GOLFER"));
          didCreateATable = true;
          logger.info("Golfer Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "course", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("COURSE"));
          didCreateATable = true;
          logger.info("Course Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "ratings", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("RATINGS"));
          didCreateATable = true;
          logger.info("Ratings Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "scores", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("SCORES"));
          didCreateATable = true;
          logger.info("Scores Table Created.");
        }
        if (didCreateATable) {
          ResultSet rsSchemas = databaseMetaData.getCatalogs();
          while (rsSchemas.next()) {
            if (dbName.equals(rsSchemas.getString(1))) {
              logger.warn("Used database: '{}' to Create Tables.", dbName);
            }
          }
        }
        rsDbname.close();
        connection.commit();
        connection.close();
      } catch (SQLException se) {
        se.printStackTrace();
      }
    });
    setupSql(entityManager);
  }

  public EntityManagerFactory getEmf() {
    return emf;
  }
}
