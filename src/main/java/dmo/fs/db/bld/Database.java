package dmo.fs.db.bld;


import dmo.fs.db.db2.DbIbmDB2;
import dmo.fs.db.fac.DbConfiguration.DbTypes;
import dmo.fs.db.h2.DbH2;
import dmo.fs.db.mariadb.DbMariadb;
import dmo.fs.db.mssql.DbMssql;
import dmo.fs.db.ora.DbOracle;
import dmo.fs.db.postgres.DbPostgres;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Database {
  private final static Logger logger = LogManager.getLogger(Database.class.getName());
  private final String mode;
  private final String dbName;
  private final String persistenceUnitName;
  private final DatabaseBuild databaseBuild;

  public String getdbName() {
    return dbName;
  }

  public String getMode() {
    return mode;
  }

  public String getPersistenceUnitName() {
    return persistenceUnitName;
  }

  public DatabaseBuild getDatabaseBuild() {
    return databaseBuild;
  }

  private Database(DatabaseBuilder builder) {
    this.dbName = builder.dbName;
    this.mode = builder.mode;
    this.persistenceUnitName = builder.persistenceUnitName;
    this.databaseBuild = builder.databaseBuild;
  }

  public static class DatabaseBuilder {
    private final String mode;
    private final String dbName;
    private final String persistenceUnitName;
    private DatabaseBuild databaseBuild;

    public DatabaseBuilder(String mode, String dbName, String persistenceUnitName) {
      this.mode = mode;
      this.dbName = dbName;
      this.persistenceUnitName = parseUnitName(persistenceUnitName);
    }

    public Database build() {
      if ((DbTypes.MARIADB.db).equals(persistenceUnitName)) {
        databaseBuild = new DbMariadb();
      } else if ((DbTypes.POSTGRES.db).equals(persistenceUnitName)) {
        databaseBuild = new DbPostgres();
      } else if ((DbTypes.H2.db).equals(persistenceUnitName)) {
        databaseBuild = new DbH2();
      } else if ((DbTypes.ORACLE.db).equals(persistenceUnitName)) {
        databaseBuild = new DbOracle();
      } else if ((DbTypes.MSSQL.db).equals(persistenceUnitName)) {
        databaseBuild = new DbMssql();
      } else if ((DbTypes.IBMDB2.db).equals(persistenceUnitName)) {
        databaseBuild = new DbIbmDB2();
      }
      return new Database(this);
    }

    private static String parseUnitName(String persistenceUnit) {
      return persistenceUnit.replaceFirst("(\\.dev|\\.prod)$", "");
    }
  }
}
