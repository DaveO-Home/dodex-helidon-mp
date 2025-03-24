package dmo.fs.db;

import dmo.fs.db.bld.Database;
import dmo.fs.db.bld.DatabaseBuild;
import dmo.fs.db.srv.DodexService;
import dmo.fs.util.DodexUtil;
import dmo.fs.util.MessageUser;
import dmo.fs.util.MessageUserImpl;
import jakarta.enterprise.context.SessionScoped;
import jakarta.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serial;
import java.io.Serializable;

@SessionScoped
public abstract class DodexDatabaseBase extends DodexService implements DodexDatabase, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final static Logger logger =
      LogManager.getLogger(DodexDatabaseBase.class.getName());

    protected static EntityManager entityManager;
    static protected String dbName;
    protected String persistenceUnitName;

    private DatabaseBuild databaseBuild;

    public DodexDatabaseBase() {
    }

    public void databaseSetup() throws InterruptedException {

        persistenceUnitName =
          entityManager.getEntityManagerFactory().getProperties().get("hibernate.persistenceUnitName").toString();

        Database database = new Database.DatabaseBuilder(
          DodexUtil.getMode(), dbName, persistenceUnitName).build();

        databaseBuild = database.getDatabaseBuild();

        if (databaseBuild == null) {
            throw new InterruptedException("DatabaseBuild is null: Is the Persistence Unit set properly in " +
              "'DbConfiguration.java'? mode/dbName/pu " +
              DodexUtil.getMode() + " -- " + dbName + " -- " + persistenceUnitName + " -- " + database);
        }
    }

    public abstract void configDatabase();

    @Override
    public MessageUser createMessageUser() {
        return new MessageUserImpl();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public DatabaseBuild
    getDatabaseBuild() {
        return databaseBuild;
    }
}
