
package dmo.fs.db;

import dmo.fs.db.bld.DatabaseBuild;

import java.io.Serializable;

public class DbOracle implements Serializable, DatabaseBuild {

    protected enum CreateTable {
        CREATEUSERS("create table dodex.users (" +
            "id INTEGER GENERATED ALWAYS AS IDENTITY INCREMENT BY 1 PRIMARY KEY," +
            "name varchar2(255) not null," +
            "password varchar2(255) not null," +
            "ip varchar2(255) not null," +
            "last_login TIMESTAMP not null)"),
        CREATENAMEIDX(
            "CREATE UNIQUE INDEX IF NOT EXISTS USERS_NAME_IDX ON DODEX.USERS (NAME)"),
        CREATEPASSWORDIDX(
            "CREATE UNIQUE INDEX IF NOT EXISTS USERS_PASSWORD_IDX ON DODEX.USERS (PASSWORD)"),
        CREATEMESSAGES("create table messages (" +
            "id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
            "message clob not null," +
            "from_handle varchar2(255) not null," +
            "post_date TIMESTAMP not null)"),
        CREATEUNDELIVERED("create table undelivered (" +
            "user_id integer, message_id integer," +
            "CONSTRAINT undelivered_user_id_foreign FOREIGN KEY (user_id) REFERENCES users (id)," +
            "CONSTRAINT undelivered_message_id_foreign FOREIGN KEY (message_id) REFERENCES messages (id))"),
        CREATEGROUPS("CREATE TABLE groups (" +
            "id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY," +
            "NAME VARCHAR2(24) NOT NULL," +
            "OWNER INTEGER NOT NULL," +
            "CREATED TIMESTAMP NOT NULL," +
            "UPDATED TIMESTAMP)"),
        CREATEMEMBER("CREATE TABLE member (" +
            "GROUP_ID INTEGER NOT NULL," +
            "USER_ID INTEGER NOT NULL," +
            "PRIMARY KEY (GROUP_ID,USER_ID)," +
            "CONSTRAINT U_fk_MEMBER_GROUP FOREIGN KEY (GROUP_ID) REFERENCES groups (ID)," +
            "CONSTRAINT U_fk_MEMBER_USER FOREIGN KEY (USER_ID) REFERENCES users (ID))");

        String sql;

        CreateTable(String sql) {
            this.sql = sql;
        }
    }

    public DbOracle() {
        super();
    }

    public String getCreateTable(String table) {
        return CreateTable.valueOf("CREATE" + table.toUpperCase()).sql;
    }
}
