package dmo.fs.db;

import dmo.fs.db.fac.DbConfiguration;
import dmo.fs.util.DodexUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
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

import static dmo.fs.db.DbIbmDB2.*;

@ApplicationScoped
public class DodexDatabaseIbmDB2 extends DodexDatabaseBase implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  private final static Logger logger = LogManager.getLogger(DodexDatabaseIbmDB2.class.getName());
  /*
    Change in DbConfiguration, to swap databases and modes(dev/prod)
   */
  @PersistenceUnit(unitName = DbConfiguration.pu)
  EntityManagerFactory emf;

  public DodexDatabaseIbmDB2() {
    super();
  }

  @Override
  public void entityManagerSetup() {
    entityManager = emf.createEntityManager();

    Session session = entityManager.unwrap(Session.class);
    SessionFactory sessionFactory = session.getSessionFactory();
    Map<String, Object> properties = sessionFactory.getProperties();
    Object sessionDbname = properties.get("dbname");
    dbName = sessionDbname != null ? sessionDbname.toString() : "";
    Object mode = properties.get("mode");
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
}
