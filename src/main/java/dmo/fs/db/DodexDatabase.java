package dmo.fs.db;

import dmo.fs.db.bld.DatabaseBuild;
import dmo.fs.util.MessageUser;
import jakarta.websocket.Session;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public interface DodexDatabase {

	DatabaseBuild getDatabaseBuild();
	void databaseSetup() throws InterruptedException;
	void configDatabase() throws InterruptedException;
	void entityManagerSetup();

	Long deleteUser(Session ws, MessageUser messageUser)
			throws SQLException, InterruptedException;

	Long addMessage(Session ws, MessageUser messageUser, String message)
			throws SQLException, InterruptedException;

	void addUndelivered(Session ws, MessageUser messageUser, List<String> undelivered, Long messageId)
			throws SQLException, InterruptedException;

	Map<String, Integer> processUserMessages(Session ws, MessageUser messageUser)
			throws Exception;

	MessageUser createMessageUser();

	MessageUser selectUser(MessageUser messageUser, Session ws)
			throws IOException;

	String buildUsersJson(MessageUser messageUser)
			throws Exception;
}
