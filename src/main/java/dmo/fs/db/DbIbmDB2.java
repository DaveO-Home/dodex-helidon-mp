
package dmo.fs.db;

import dmo.fs.db.bld.DatabaseBuild;

import java.io.Serializable;

public class DbIbmDB2 implements Serializable, DatabaseBuild {

    protected final static String CHECKUSERSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='USERS'";
    protected final static String CHECKMESSAGESQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='MESSAGES'";
    protected final static String CHECKUNDELIVEREDSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='UNDELIVERED'";
    protected final static String CHECKGROUPSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='GROUPS'";
    protected final static String CHECKMEMBERSQL = "select tabname from syscat.tables where tabschema='DB2INST1' and tabname='MEMBER'";
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
                "REFERENCES USERS (ID) ON DELETE RESTRICT)"
        );

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
