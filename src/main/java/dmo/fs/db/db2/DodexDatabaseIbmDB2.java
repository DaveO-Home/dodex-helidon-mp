package dmo.fs.db.db2;

import dmo.fs.db.DodexDatabaseBase;
import dmo.fs.util.DodexUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
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

import static dmo.fs.db.db2.DbIbmDB2.*;

@ApplicationScoped
public class DodexDatabaseIbmDB2 extends DodexDatabaseBase implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  private final static Logger logger = LogManager.getLogger(DodexDatabaseIbmDB2.class.getName());

  private static EntityManagerFactory emf;

  public DodexDatabaseIbmDB2() {
    super();
  }

  @Override
  public void entityManagerSetup() {
    if(emf == null) {
      emf = Persistence.createEntityManagerFactory("ibmdb2." + DodexUtil.getMode());
    }

    entityManager = emf.createEntityManager();

    Session session = entityManager.unwrap(Session.class);
    SessionFactory sessionFactory = session.getSessionFactory();
    Map<String, Object> properties = sessionFactory.getProperties();
    Object sessionDbname = properties.get("dbname");
    dbName = sessionDbname != null ? sessionDbname.toString() : "";
    Object mode = properties.get("mode");
  }

  @Override
  public void entityManagerSetup(EntityManagerFactory emf) {

  }

  @Override
  public void configDatabase() {
    String[] types = {"TABLE"};
    Session session = entityManager.unwrap(Session.class);
    session.doWork(connection -> {
      try {
        connection.setAutoCommit(false);
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        boolean didCreateATable = false;

        boolean isTableFound = connection.createStatement().executeQuery(CHECKUSERSQL).next();

        if (!isTableFound) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("USERS"));
          didCreateATable = true;
          logger.info("Users Table Created.");
        }

        isTableFound = connection.createStatement().executeQuery(CHECKMESSAGESQL).next();
        if (!isTableFound) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("MESSAGES"));
          didCreateATable = true;
          logger.info("Messages Table Created.");
        }

        isTableFound = connection.createStatement().executeQuery(CHECKUNDELIVEREDSQL).next();
        if (!isTableFound) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("UNDELIVERED"));
          didCreateATable = true;
          logger.info("Undelivered Table Created.");
        }

        isTableFound = connection.createStatement().executeQuery(CHECKGROUPSQL).next();
        if (!isTableFound) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("GROUPS"));
          didCreateATable = true;
          logger.info("Groups Table Created.");
        }

        isTableFound = connection.createStatement().executeQuery(CHECKMEMBERSQL).next();
        if (!isTableFound) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("MEMBER"));
          didCreateATable = true;
          logger.info("Member Table Created.");
        }
        isTableFound = connection.createStatement().executeQuery(CHECKGOLFERSQL).next();
        if (!isTableFound) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("GOLFER"));
          didCreateATable = true;
          logger.info("Golfer Table Created.");
        }
        isTableFound = connection.createStatement().executeQuery(CHECKCOURSESQL).next();
        if (!isTableFound) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("COURSE"));
          didCreateATable = true;
          logger.info("Course Table Created.");
        }
        isTableFound = connection.createStatement().executeQuery(CHECKRATINGSSQL).next();
        if (!isTableFound) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("RATINGS"));
          didCreateATable = true;
          logger.info("Ratings Table Created.");
        }
        isTableFound = connection.createStatement().executeQuery(CHECKSCORESSQL).next();
        if (!isTableFound) {
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
        connection.commit();
        connection.close();
      } catch (SQLException se) {
        se.printStackTrace();
          connection.rollback();
          connection.close();
      }
    });
    setupSql(entityManager);
  }

  public EntityManagerFactory getEmf() {
    return emf;
  }
}
