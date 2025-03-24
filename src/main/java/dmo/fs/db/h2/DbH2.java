
package dmo.fs.db.h2;

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
				"CONSTRAINT U_fk_MEMBER_USER FOREIGN KEY (USER_ID) REFERENCES users (ID))"),
		CREATEGOLFER("CREATE TABLE IF NOT EXISTS golfer (" +
				"PIN CHARACTER(8) primary key NOT NULL," +
				"FIRST_NAME VARCHAR(32) NOT NULL," +
				"LAST_NAME VARCHAR(32) NOT NULL," +
				"HANDICAP REAL DEFAULT 0.0," +
				"COUNTRY CHARACTER(2) DEFAULT 'US' NOT NULL," +
				"STATE CHARACTER(2) DEFAULT 'NV' NOT NULL," +
				"OVERLAP_YEARS BOOLEAN DEFAULT false," +
				"PUBLIC_DISPLAY BOOLEAN DEFAULT true," +
				"LAST_LOGIN TIMESTAMP)"),
		CREATECOURSE("CREATE TABLE IF NOT EXISTS course (" +
				"COURSE_SEQ INTEGER primary key auto_increment NOT NULL," +
				"COURSE_NAME VARCHAR(128) NOT NULL," +
				"COURSE_COUNTRY VARCHAR(128) NOT NULL," +
				"COURSE_STATE CHARACTER(2) NOT NULL )"),
		CREATERATINGS("CREATE TABLE IF NOT EXISTS ratings (" +
				"COURSE_SEQ INTEGER NOT NULL," +
				"TEE INTEGER NOT NULL," +
				"TEE_COLOR VARCHAR(16)," +
				"TEE_RATING REAL NOT NULL," +
				"TEE_SLOPE INTEGER NOT NULL," +
				"TEE_PAR INTEGER DEFAULT '72' NOT NULL, PRIMARY KEY (COURSE_SEQ, TEE)," +
				"CONSTRAINT FK_COURSE_SEQ_RATING FOREIGN KEY ( COURSE_SEQ ) " +
				"REFERENCES course ( COURSE_SEQ ))"),
		CREATESCORES("CREATE TABLE IF NOT EXISTS scores (" +
				"PIN CHARACTER(8) NOT NULL," +
				"GROSS_SCORE INTEGER NOT NULL," +
				"NET_SCORE REAL," +
				"ADJUSTED_SCORE INTEGER NOT NULL," +
				"TEE_TIME TEXT NOT NULL," +
				"HANDICAP REAL," +
				"COURSE_SEQ INTEGER," +
				"COURSE_TEES INTEGER," +
				"USED CHARACTER(1), CONSTRAINT FK_SCORES_SEQ_COURSE FOREIGN KEY ( COURSE_SEQ )" +
				"REFERENCES course ( COURSE_SEQ ), CONSTRAINT FK_GOLFER_PIN_SCORES FOREIGN KEY ( PIN ) " +
				"REFERENCES golfer ( PIN ))");

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
