
package dmo.fs.db.db2;

import dmo.fs.db.bld.DatabaseBuild;

import java.io.Serializable;

public class DbIbmDB2 implements Serializable, DatabaseBuild {

    protected final static String CHECKUSERSQL = "SELECT name FROM SYSIBM.SYSTABLES WHERE NAME = 'USERS' and type = 'T'";
    protected final static String CHECKMESSAGESQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='MESSAGES'";
    protected final static String CHECKUNDELIVEREDSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='UNDELIVERED'";
    protected final static String CHECKGROUPSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='GROUPS'";
    protected final static String CHECKMEMBERSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='MEMBER'";
    protected final static String CHECKGOLFERSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='GOLFER'";
    protected final static String CHECKCOURSESQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='COURSE'";
    protected final static String CHECKRATINGSSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='RATINGS'";
    protected final static String CHECKSCORESSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='SCORES'";

    protected enum CreateTable {
        CREATEUSERS(
          "CREATE TABLE USERS (" +
            "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
            "NAME VARCHAR(255) NOT NULL, " +
            "PASSWORD VARCHAR(255) NOT NULL, " +
            "IP VARCHAR(255) NOT NULL, " +
            "LAST_LOGIN TIMESTAMP(12) NOT NULL)"),
        CREATEUSERSINDEX(
          "CREATE UNIQUE INDEX XUSERS " +
            "ON USERS " +
            "(NAME ASC, PASSWORD ASC)"),
        CREATEMESSAGES(
          "CREATE TABLE MESSAGES (" +
            "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
            "MESSAGE VARCHAR(10240) NOT NULL," +
            "FROM_HANDLE VARCHAR(255) NOT NULL," +
            "POST_DATE TIMESTAMP(12) NOT NULL)"),
        CREATEUNDELIVERED(
          "CREATE TABLE UNDELIVERED (" +
            "USER_ID INTEGER NOT NULL," +
            "MESSAGE_ID INTEGER NOT NULL, " +
            "FOREIGN KEY (USER_ID) " +
            "REFERENCES USERS (ID) ON DELETE RESTRICT, " +
            "FOREIGN KEY (MESSAGE_ID) " +
            "REFERENCES MESSAGES (ID) ON DELETE RESTRICT)"
        ),
        CREATEGROUPS(
          "CREATE TABLE GROUPS (" +
            "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
            "NAME VARCHAR(255) NOT NULL, " +
            "OWNER INTEGER NOT NULL, " +
            "CREATED TIMESTAMP(12) NOT NULL, " +
            "UPDATED TIMESTAMP(12))"),
        CREATEMEMBER(
          "CREATE TABLE MEMBER (" +
            "GROUP_ID INTEGER NOT NULL," +
            "USER_ID INTEGER NOT NULL, " +
            "FOREIGN KEY (GROUP_ID) " +
            "REFERENCES GROUPS (ID) ON DELETE RESTRICT, " +
            "FOREIGN KEY (USER_ID) " +
            "REFERENCES USERS (ID) ON DELETE RESTRICT)"),
        CREATEGOLFER(
          "CREATE TABLE IF NOT EXISTS GOLFER (" +
            "PIN VARCHAR(8) PRIMARY KEY NOT NULL," +
            "FIRST_NAME VARCHAR(32) NOT NULL," +
            "LAST_NAME VARCHAR(32) NOT NULL," +
            "HANDICAP REAL DEFAULT 0.0," +
            "COUNTRY CHAR(2) DEFAULT 'US' NOT NULL," +
            "STATE CHAR(2) DEFAULT 'NV' NOT NULL," +
            "OVERLAP_YEARS BOOLEAN DEFAULT 0," +
            "PUBLIC_DISPLAY BOOLEAN DEFAULT 0," +
            "LAST_LOGIN Timestamp(12)," +
            "CONSTRAINT golfer_names_unique UNIQUE (LAST_NAME, FIRST_NAME))"),
        CREATECOURSE(
          "CREATE TABLE IF NOT EXISTS COURSE (" +
            "COURSE_SEQ INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
            "COURSE_NAME VARCHAR(128) NOT NULL," +
            "COURSE_COUNTRY VARCHAR(128) NOT NULL," +
            "COURSE_STATE CHAR(2) NOT NULL)"),
        CREATERATINGS(
          "CREATE TABLE IF NOT EXISTS RATINGS (" +
            "COURSE_SEQ INTEGER NOT NULL," +
            "TEE INTEGER NOT NULL," +
            "TEE_COLOR VARCHAR(16)," +
            "TEE_RATING REAL NOT NULL," +
            "TEE_SLOPE INTEGER NOT NULL," +
            "TEE_PAR INTEGER DEFAULT '72' NOT NULL, PRIMARY KEY (COURSE_SEQ, TEE)," +
            "CONSTRAINT fk_course_ratings " +
            "FOREIGN KEY (COURSE_SEQ) " +
            "REFERENCES COURSE (COURSE_SEQ) " +
            "ON DELETE RESTRICT " +
            "ON UPDATE RESTRICT)"),
        CREATESCORES(
          "CREATE TABLE IF NOT EXISTS SCORES (" +
            "PIN VARCHAR(8) NOT NULL," +
            "GROSS_SCORE INTEGER NOT NULL," +
            "NET_SCORE REAL," +
            "ADJUSTED_SCORE INTEGER NOT NULL," +
            "TEE_TIME Timestamp(12) NOT NULL," +
            "HANDICAP REAL," +
            "COURSE_SEQ INTEGER," +
            "COURSE_TEES INTEGER," +
            "USED CHAR(1)," +
            "CONSTRAINT fk_course_scores " +
            "FOREIGN KEY (COURSE_SEQ) " +
            "REFERENCES COURSE (COURSE_SEQ) " +
            "ON DELETE RESTRICT " +
            "ON UPDATE RESTRICT," +
            "CONSTRAINT fk_golfer_scores " +
            "FOREIGN KEY (PIN) " +
            "REFERENCES GOLFER (PIN) " +
            "ON DELETE RESTRICT " +
            "ON UPDATE RESTRICT)");

        String sql;

        CreateTable(String sql) {
            this.sql = sql;
        }
    }

    public DbIbmDB2() {
        super();
    }

    public String getCreateTable(String table) {
        return CreateTable.valueOf("CREATE" + table.toUpperCase()).sql;
    }
}
