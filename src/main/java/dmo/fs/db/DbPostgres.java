
package dmo.fs.db;

import dmo.fs.db.bld.DatabaseBuild;

import java.io.Serializable;

public class DbPostgres implements Serializable, DatabaseBuild {

	protected enum CreateTable {
		CREATEUSERS(
			"CREATE SEQUENCE IF NOT EXISTS public.users_id_seq INCREMENT 1 START 19 MINVALUE 1 MAXVALUE 2147483647 CACHE 1; " +
			"ALTER SEQUENCE public.users_id_seq OWNER TO dummy;" +
			"CREATE TABLE public.users" +
				"(id integer NOT NULL DEFAULT nextval('users_id_seq'::regclass)," +
				"name character varying(255) COLLATE pg_catalog.\"default\"," +
				"password character varying(255) COLLATE pg_catalog.\"default\"," +
				"ip character varying(255) COLLATE pg_catalog.\"default\"," +
				"last_login timestamp with time zone," +
				"CONSTRAINT users_pkey PRIMARY KEY (id)," +
				"CONSTRAINT users_name_unique UNIQUE (name)," +
				"CONSTRAINT users_password_unique UNIQUE (password))" +
				"WITH (OIDS = FALSE) TABLESPACE pg_default;" +		
			"ALTER TABLE public.users OWNER to dummy;"),
		CREATEMESSAGES(
			"CREATE SEQUENCE IF NOT EXISTS public.messages_id_seq INCREMENT 1 START 4 MINVALUE 1 MAXVALUE 2147483647 CACHE 1;" +
			"ALTER SEQUENCE public.messages_id_seq OWNER TO dummy;" +
			"CREATE TABLE public.messages" + 
				"(id integer NOT NULL DEFAULT nextval('messages_id_seq'::regclass)," +
				"message text COLLATE pg_catalog.\"default\"," +
				"from_handle character varying(255) COLLATE pg_catalog.\"default\"," +
				"post_date timestamp with time zone," +
				"CONSTRAINT messages_pkey PRIMARY KEY (id))" +
				"WITH (OIDS = FALSE) TABLESPACE pg_default;" +
			"ALTER TABLE public.messages OWNER to dummy;"),
		CREATEUNDELIVERED(
			"CREATE TABLE public.undelivered" +
				"(user_id integer, message_id integer," +
				"CONSTRAINT undelivered_message_id_foreign FOREIGN KEY (message_id)" +
				"REFERENCES public.messages (id) MATCH SIMPLE " +
				"ON UPDATE NO ACTION ON DELETE NO ACTION NOT VALID," +
				"CONSTRAINT undelivered_user_id_foreign FOREIGN KEY (user_id)" +
				"REFERENCES public.users (id) MATCH SIMPLE " +
				"ON UPDATE NO ACTION ON DELETE NO ACTION NOT VALID)" +
				"WITH (OIDS = FALSE) TABLESPACE pg_default;" +
			"ALTER TABLE public.undelivered OWNER to dummy;"),
		CREATEGROUPS(
			"CREATE TABLE IF NOT EXISTS public.groups (" +
				"id integer GENERATED ALWAYS AS IDENTITY, " +
				"name varchar(24) COLLATE pg_catalog.\"default\"," +
				"owner INTEGER NOT NULL DEFAULT 0," +
				"created timestamp with time zone NOT NULL," +
				"updated timestamp with time zone DEFAULT NULL," +
				"CONSTRAINT groups_pkey PRIMARY KEY (id)," +
				"CONSTRAINT name_ukey UNIQUE (name))" +
				"WITH (OIDS = FALSE) TABLESPACE pg_default;" +
				"ALTER TABLE public.groups OWNER to dummy;"),
		CREATEMEMBER(
			"CREATE TABLE IF NOT EXISTS public.member (" +
				"GROUP_ID INTEGER NOT NULL DEFAULT 0," +
				"USER_ID INTEGER NOT NULL DEFAULT 0," +
				"CONSTRAINT member_group_id_foreign FOREIGN KEY (group_id)" +
				"REFERENCES public.groups (id) MATCH SIMPLE " +
				"ON UPDATE NO ACTION ON DELETE NO ACTION NOT VALID," +
				"CONSTRAINT member_user_id_foreign FOREIGN KEY (user_id)" +
				"REFERENCES public.users (id) MATCH SIMPLE " +
				"ON UPDATE NO ACTION ON DELETE NO ACTION NOT VALID)" +
				"WITH (OIDS = FALSE) TABLESPACE pg_default;" +
				"ALTER TABLE public.member OWNER to dummy;");

        String sql;

        CreateTable(String sql) {
            this.sql = sql;
        }
    };

	public DbPostgres() {
		super();
	}

	public String getCreateTable(String table) {
		return CreateTable.valueOf("CREATE"+table.toUpperCase()).sql;
	}
}
