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

@ApplicationScoped
public class DodexDatabaseOracle extends DodexDatabaseBase implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  private final static Logger logger = LogManager.getLogger(DodexDatabaseOracle.class.getName());
  /*
    Change in DbConfiguration, to swap databases and modes(dev/prod)
   */
  @PersistenceUnit(unitName = DbConfiguration.pu)
  EntityManagerFactory emf;

  public DodexDatabaseOracle() {
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

        ResultSet rsDbname = databaseMetaData.getTables(dbName, null, "USERS", types);

        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("USERS"));
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("NAMEIDX"));
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("PASSWORDIDX"));
          didCreateATable = true;
          logger.info("Users Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "MESSAGES", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("MESSAGES"));
          didCreateATable = true;
          logger.info("Messages Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "UNDELIVERED", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("UNDELIVERED"));
          didCreateATable = true;
          logger.info("Undelivered Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "GROUPS", types);
        if (!rsDbname.next()) {
          connection.createStatement().execute(getDatabaseBuild().getCreateTable("GROUPS"));
          didCreateATable = true;
          logger.info("Groups Table Created.");
        }
        rsDbname = databaseMetaData.getTables(dbName, null, "MEMBER", types);
        if (!rsDbname.next()) {
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
        rsDbname.close();
        connection.commit();
        connection.close();
      } catch (SQLException se) {
        se.printStackTrace();
      }
    });
    setupSql(entityManager);
  }
}
