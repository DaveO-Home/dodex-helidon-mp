
package dmo.fs.db;

import dmo.fs.db.bld.DatabaseBuild;

import java.io.Serializable;

public class DbH2 implements Serializable, DatabaseBuild {

	protected enum CreateTable {
		CREATEUSERS("create table IF NOT EXISTS users (" +
			"ID int auto_increment NOT NULL PRIMARY KEY," +
			"NAME varchar(255) not null unique," +
			"PASSWORD varchar(255) not null unique," +
			"IP varchar(255) not null," +
			"LAST_LOGIN TIMESTAMP not null)"),
		CREATEMESSAGES("create table IF NOT EXISTS messages (" +
			"ID int auto_increment NOT NULL PRIMARY KEY," +
			"MESSAGE clob not null," +
			"FROM_HANDLE varchar(255) not null," +
			"POST_DATE TIMESTAMP not null)"),
		CREATEUNDELIVERED("create table IF NOT EXISTS undelivered (" +
			"USER_ID int, MESSAGE_ID int," +
			"CONSTRAINT undelivered_user_id_foreign FOREIGN KEY (USER_ID) REFERENCES users (ID)," +
			"CONSTRAINT undelivered_message_id_foreign FOREIGN KEY (MESSAGE_ID) REFERENCES messages (ID))"),
		CREATEGROUPS("CREATE TABLE IF NOT EXISTS groups (" +
				"ID INTEGER primary key auto_increment NOT NULL," +
				"NAME CHARACTER(24) UNIQUE NOT NULL," +
				"OWNER INTEGER NOT NULL," +
				"CREATED TIMESTAMP NOT NULL," +
				"UPDATED TIMESTAMP)"),
		CREATEMEMBER("CREATE TABLE IF NOT EXISTS member (" +
				"GROUP_ID INTEGER NOT NULL DEFAULT 0," +
				"USER_ID INTEGER NOT NULL DEFAULT 0," +
				"PRIMARY KEY (GROUP_ID,USER_ID)," +
				"CONSTRAINT U_fk_MEMBER_GROUP FOREIGN KEY (GROUP_ID) REFERENCES groups (ID)," +
				"CONSTRAINT U_fk_MEMBER_USER FOREIGN KEY (USER_ID) REFERENCES users (ID))");

		String sql;		

        CreateTable(String sql) {
            this.sql = sql;
        }
    }

	public DbH2() {
		super();
	}

	public String getCreateTable(String table) {
		return CreateTable.valueOf("CREATE"+table.toUpperCase()).sql;
	}
}
