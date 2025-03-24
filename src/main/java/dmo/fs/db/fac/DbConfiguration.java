package dmo.fs.db.fac;

import dmo.fs.db.*;
import dmo.fs.db.db2.DodexDatabaseIbmDB2;
import dmo.fs.db.h2.DodexDatabaseH2;
import dmo.fs.db.mariadb.DodexDatabaseMariadb;
import dmo.fs.db.mssql.DodexDatabaseMssql;
import dmo.fs.db.ora.DodexDatabaseOracle;
import dmo.fs.db.postgres.DodexDatabasePostgres;
import dmo.fs.util.ColorUtilConstants;
import dmo.fs.util.DodexUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public abstract class DbConfiguration {
    private final static Logger logger = LogManager.getLogger(DbConfiguration.class.getName());

    public final static String pu = "h2.dev";

    private static final Map<String, String> map = new ConcurrentHashMap<>();
    protected static Properties properties = new Properties();
    private static Boolean isUsingH2 = false;
    private static Boolean isUsingPostgres = false;
    private static Boolean isUsingOracle = false;
    private static Boolean isUsingMssql = false;
    private static Boolean isUsingMariadb = false;
    private static Boolean isUsingIbmDB2 = false;
    private static String defaultDb = "h2";
    private static final DodexUtil dodexUtil = new DodexUtil();
    private static DodexDatabase dodexDatabase;

    public enum DbTypes {
        POSTGRES("postgres"), H2("h2"), CUBRID("cubrid"), MARIADB("mariadb"), ORACLE("oracle"), MSSQL("mssql"),
            IBMDB2("ibmdb2"), CASSANDRA("cassandra"), FIREBASE("firebase"), NEO4J("neo4j"), DEFAULT("");

        public final String db;

        DbTypes(String db) {
            this.db = db;
        }
    }

    public static boolean isUsingH2() {
        return isUsingH2;
    }

    public static boolean isUsingPostgres() {
        return isUsingPostgres;
    }

    public static boolean isUsingOracle() {
        return isUsingOracle;
    }
    public static boolean isUsingMssql() {
        return isUsingMssql;
    }

    public static boolean isUsingMariadb() {
        return isUsingMariadb;
    }

    public static boolean isUsingIbmDB2() {
        return isUsingIbmDB2;
    }

    public DbConfiguration() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultDb() throws InterruptedException, IOException, SQLException {
        defaultDb = dodexUtil.getDefaultDb().toLowerCase();
        String mode = DodexUtil.getMode();
        String dev = "dev";

        if(dodexDatabase == null) {
            logger.info("{}Using Database: {}.{}{}", ColorUtilConstants.BLUE_BRIGHT, defaultDb, mode, ColorUtilConstants.RESET);
        }

        try {
            if (defaultDb.equals(DbTypes.H2.db) || defaultDb.equals(DbTypes.DEFAULT.db)) {
                dodexDatabase = CDI.current().select(DodexDatabaseH2.class).get();
                isUsingH2 = true;
            } else if (defaultDb.equals(DbTypes.POSTGRES.db)) {
                dodexDatabase = CDI.current().select(DodexDatabasePostgres.class).get();
                isUsingPostgres = true;
            } else if (defaultDb.equals(DbTypes.ORACLE.db)) {
                dodexDatabase = CDI.current().select(DodexDatabaseOracle.class).get();
                isUsingOracle = true;
            } else if (defaultDb.equals(DbTypes.MARIADB.db)) {
                dodexDatabase = CDI.current().select(DodexDatabaseMariadb.class).get();
                isUsingMariadb = true;
            } else if (defaultDb.equals(DbTypes.IBMDB2.db)) {
                dodexDatabase = CDI.current().select(DodexDatabaseIbmDB2.class).get();
                isUsingIbmDB2 = true;
            } else if (defaultDb.equals(DbTypes.MSSQL.db)) {
                dodexDatabase = CDI.current().select(DodexDatabaseMssql.class).get();
                isUsingMssql = true;
            }
        } catch (Exception exception) {
            throw exception;
        }

        return (T) dodexDatabase;
    }

    public static String getDb() {
        return defaultDb;
    }
}
