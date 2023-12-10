
package dmo.fs.db;

import dmo.fs.db.bld.DatabaseBuild;

import java.io.Serializable;

public class DbMariadb implements Serializable, DatabaseBuild {

	private enum CreateTable {
		CREATEUSERS(
				"CREATE TABLE users (" +
						"id INT NOT NULL AUTO_INCREMENT," +
						"name VARCHAR(255) CHARACTER SET utf8mb4 collate utf8mb4_general_ci NOT NULL," +
						"password VARCHAR(255) NOT NULL," +
						"ip VARCHAR(255) NOT NULL," +
						"last_login DATETIME NOT NULL," +
						"PRIMARY KEY (id)," +
						"UNIQUE INDEX U_name_password_UNIQUE (name ASC, password ASC));"
				),
		CREATEUSERSSEQ("CREATE SEQUENCE IF NOT EXISTS users_SEQ START WITH 1 INCREMENT BY 50;"),
		CREATEMESSAGES(
				"CREATE TABLE messages (" +
						"id INT NOT NULL AUTO_INCREMENT," +
						"message MEDIUMTEXT NOT NULL," +
						"from_handle VARCHAR(255) CHARACTER SET utf8mb4 collate utf8mb4_general_ci NOT NULL," +
						"post_date DATETIME NOT NULL," +
						"PRIMARY KEY (id));"
				),
		CREATEMESSAGESSEQ("CREATE SEQUENCE IF NOT EXISTS messages_SEQ START WITH 1 INCREMENT BY 50;"),
		CREATEUNDELIVERED(
				"CREATE TABLE undelivered (" +
						"user_id INT NOT NULL," +
						"message_id INT NOT NULL," +
						"INDEX U_fk_undelivered_users_idx (user_id ASC)," +
						"INDEX U_fk_undelivered_messages_idx (message_id ASC)," +
						"CONSTRAINT U_fk_undelivered_users " +
						"FOREIGN KEY (user_id) " +
						"REFERENCES users (id) " +
						"ON DELETE NO ACTION " +
						"ON UPDATE NO ACTION," +
						"CONSTRAINT U_fk_undelivered_messages " +
						"FOREIGN KEY (message_id) " +
						"REFERENCES messages (id) " +
						"ON DELETE NO ACTION " +
						"ON UPDATE NO ACTION);"),
		CREATEGROUPS(
				"CREATE TABLE groups (" +
						"ID INTEGER primary key auto_increment NOT NULL," +
						"NAME VARCHAR(24) NOT NULL," +
						"OWNER INTEGER NOT NULL DEFAULT 0," +
						"CREATED DATETIME NOT NULL DEFAULT current_timestamp()," +
						"UPDATED DATETIME DEFAULT NULL ON UPDATE current_timestamp()," +
						"UNIQUE KEY unique_on_name (NAME))"
		),
		CREATEMEMBER(
				"CREATE TABLE member (" +
						"GROUP_ID INTEGER NOT NULL DEFAULT 0," +
						"USER_ID INTEGER NOT NULL DEFAULT 0," +
						"PRIMARY KEY (GROUP_ID,USER_ID)," +
						"KEY U_fk_MEMBER_USER_idx (USER_ID)," +
						"CONSTRAINT U_fk_MEMBER_GROUP FOREIGN KEY (GROUP_ID) REFERENCES groups (ID) ON DELETE NO ACTION ON UPDATE NO ACTION," +
						"CONSTRAINT U_fk_MEMBER_USER FOREIGN KEY (USER_ID) REFERENCES users (ID) ON DELETE NO ACTION ON UPDATE NO ACTION)"
		);

        final String sql;

        CreateTable(String sql) {
            this.sql = sql;
        }
    }

	public DbMariadb() {
		super();
	}

	public String getCreateTable(String table) {
		return  CreateTable.valueOf("CREATE"+table.toUpperCase()).sql;
	}

}
