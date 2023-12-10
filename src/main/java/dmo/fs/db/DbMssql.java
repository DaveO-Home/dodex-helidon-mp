
package dmo.fs.db;

import dmo.fs.db.bld.DatabaseBuild;

import java.io.Serializable;

public class DbMssql implements Serializable, DatabaseBuild {

    protected enum CreateTable {
        CREATEUSERS("create table dodex.users (" +
            "id INT IDENTITY (1,1) PRIMARY KEY," +
            "name varchar(255) collate LATIN1_GENERAL_100_CI_AS_SC_UTF8 not null," +
            "password varchar(255) not null," +
            "ip varchar(255) not null," +
            "last_login DATETIME not null)"),
        CREATENAMEIDX(
            "CREATE UNIQUE INDEX IF NOT EXISTS USERS_NAME_IDX ON DODEX.USERS (NAME)"),
        CREATEPASSWORDIDX(
            "CREATE UNIQUE INDEX IF NOT EXISTS USERS_PASSWORD_IDX ON DODEX.USERS (PASSWORD)"),
        CREATEMESSAGES("create table messages (" +
            "id INT IDENTITY (1,1) PRIMARY KEY," +
            "message text not null," +
            "from_handle varchar(255) collate LATIN1_GENERAL_100_CI_AS_SC_UTF8 not null," +
            "post_date DATETIME not null)"),
        CREATEUNDELIVERED("create table undelivered (" +
            "user_id int, message_id int," +
            "CONSTRAINT undelivered_user_id_foreign FOREIGN KEY (user_id) REFERENCES users (id)," +
            "CONSTRAINT undelivered_message_id_foreign FOREIGN KEY (message_id) REFERENCES messages (id))"),
        CREATEGROUPS("CREATE TABLE groups (" +
            "id INT IDENTITY (1,1) PRIMARY KEY," +
            "NAME VARCHAR(24) NOT NULL," +
            "OWNER INT NOT NULL," +
            "CREATED DATETIME NOT NULL," +
            "UPDATED DATETIME)"),
        CREATEMEMBER("CREATE TABLE member (" +
            "GROUP_ID INT NOT NULL," +
            "USER_ID INT NOT NULL," +
            "PRIMARY KEY (GROUP_ID,USER_ID)," +
            "CONSTRAINT U_fk_MEMBER_GROUP FOREIGN KEY (GROUP_ID) REFERENCES groups (ID)," +
            "CONSTRAINT U_fk_MEMBER_USER FOREIGN KEY (USER_ID) REFERENCES users (ID))");

        String sql;

        CreateTable(String sql) {
            this.sql = sql;
        }
    }

    public DbMssql() {
        super();
    }

    public String getCreateTable(String table) {
        return CreateTable.valueOf("CREATE" + table.toUpperCase()).sql;
    }
}
