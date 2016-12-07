/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.knowledge.database;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.persistence.Persistence;
import javax.persistence.Query;

import org.botlibre.Bot;
import org.botlibre.LogListener;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.jpa.metadata.xml.XMLEntityMappingsReader;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.jpa.JpaEntityManagerFactory;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.Connector;
import org.eclipse.persistence.sessions.DatasourceLogin;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.ServerSession;

/**
 * Uses JPA to store the memory to a Derby database.
 */

public class DerbyMemory extends DatabaseMemory {
	
	public DerbyMemory() {
		super();

		DATABASE_URL_PREFIX = "jdbc:derby:";
		SCHEMA_URL_PREFIX = "jdbc:derby:";
		DATABASE_URL = "jdbc:derby:Bot";
		DATABASE_TEST_URL = "jdbc:derby:Bot";
		DATABASE_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
		DATABASE_USER = "";
		DATABASE_PASSWORD = "";
		
		if (TEST) {
			this.database = DATABASE_TEST_URL.replace(DATABASE_URL_PREFIX, "");
		} else {
			this.database = DATABASE_URL.replace(DATABASE_URL_PREFIX, "");
		}
	}
	
	/**
	 * Initialize any configurable settings from the properties.
	 */
	@Override
	public void initialize(Map<String, Object> properties) {
		super.initialize(properties);
	}
	
	/**
	 * Create the database.
	 */
	public void createMemory(String database, boolean schema) {
		try {
			shutdown();
			restore(database, schema, true);
		} catch (Exception failed) {
			this.bot.log(this, failed);
			throw new RuntimeException(failed);
		}
	}
	
	/**
	 * Connect and create the EntityManager.
	 */
	public void restore(String database, boolean isSchema, boolean recreateDatabase) {
		this.isSchema = isSchema;
		try {
			if (getFactory() != null) {
				getFactory().close();
			}
			this.bot.log(this, "Restoring", Bot.FINE);
			Map<String, String> properties = new HashMap<String, String>();
			//properties.put(PersistenceUnitProperties.JDBC_DRIVER, DATABASE_DRIVER);
			if (RECREATE_DATABASE) {
				recreateDatabase = true;
			}
			String suffix = "";
			if (recreateDatabase) {
				suffix = ";create=true";
			}
			if (database.equals("")) {
				if (TEST) {
					properties.put(PersistenceUnitProperties.JDBC_URL, DATABASE_TEST_URL + suffix);
					this.database = DATABASE_TEST_URL.replace(DATABASE_URL_PREFIX, "");
				} else {
					properties.put(PersistenceUnitProperties.JDBC_URL, DATABASE_URL + suffix);
					this.database = DATABASE_URL.replace(DATABASE_URL_PREFIX, "");
				}
			} else {
				if (this.isSchema) {
					properties.put(PersistenceUnitProperties.JDBC_URL, SCHEMA_URL_PREFIX + database  + suffix);
				} else {
					properties.put(PersistenceUnitProperties.JDBC_URL, DATABASE_URL_PREFIX + database  + suffix);
				}
				this.database = database;
			}
			//properties.put(PersistenceUnitProperties.JDBC_USER, DATABASE_USER);
			//properties.put(PersistenceUnitProperties.JDBC_PASSWORD, DATABASE_PASSWORD);
			properties.put(PersistenceUnitProperties.CACHE_SIZE_DEFAULT, CACHE_SIZE);
			properties.put(PersistenceUnitProperties.CACHE_STATEMENTS, "true");
			properties.put(PersistenceUnitProperties.BATCH_WRITING, "JDBC");
			if (recreateDatabase) {
				properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.DROP_AND_CREATE);
			}
			properties.put(PersistenceUnitProperties.SESSION_CUSTOMIZER, MemorySessionCustomizer.class.getName());
			Level debugLevel = this.bot.getDebugLevel();
			String logLevel = "INFO";
			if (debugLevel == Level.ALL) {
				logLevel = "FINEST";
			} else if (debugLevel == Level.FINEST) {
				logLevel = "FINE";
			} else if (debugLevel == Level.FINE) {
				logLevel = "INFO";
			} else if (debugLevel == Level.SEVERE) {
				logLevel = "SEVER";
			} else if (debugLevel == Level.WARNING) {
				logLevel = "WARNING";
			} else if (debugLevel == Level.OFF) {
				logLevel = "OFF";
			}
			properties.put(PersistenceUnitProperties.LOGGING_LEVEL, logLevel);
			setFactory(Persistence.createEntityManagerFactory("Bot", properties));
			setEntityManager(getFactory().createEntityManager());
			
			// Increase sequence preallocation size.
			ServerSession server = this.entityManager.unwrap(ServerSession.class);
			try {
				// Clear EclipseLink XML project.
				for (Field field : XMLEntityMappingsReader.class.getDeclaredFields()) {
					if (field.getName().equals("m_orm1_0Project")
								|| field.getName().equals("m_orm2_0Project")
								|| field.getName().equals("m_eclipseLinkOrmProject")) {
						field.setAccessible(true);
						field.set(null, null);
					}
				}				
			} catch (Exception exception) {
				this.bot.log(this, exception);
			}
			server.getLogin().getSequence("SEQ_GEN").setPreallocationSize(1000);
			
			if (recreateDatabase) {
				this.entityManager.getTransaction().begin();
				try {					
					Query query = this.entityManager.createNativeQuery("ALTER TABLE relationship DROP CONSTRAINT fk_relationship_source_id");
					query.executeUpdate();
					query = this.entityManager.createNativeQuery("ALTER TABLE relationship ADD CONSTRAINT fk_relationship_source_id FOREIGN KEY (source_id) " +
								"REFERENCES vertex (id) ON DELETE CASCADE");
					query.executeUpdate();
					
					query = this.entityManager.createNativeQuery("ALTER TABLE relationship DROP CONSTRAINT fk_relationship_target_id");
					query.executeUpdate();
					query = this.entityManager.createNativeQuery("ALTER TABLE relationship ADD CONSTRAINT fk_relationship_target_id FOREIGN KEY (target_id) " +
								"REFERENCES vertex (id) ON DELETE CASCADE");
					query.executeUpdate();
					
					query = this.entityManager.createNativeQuery("ALTER TABLE relationship DROP CONSTRAINT fk_relationship_type_id");
					query.executeUpdate();
					query = this.entityManager.createNativeQuery("ALTER TABLE relationship ADD CONSTRAINT fk_relationship_type_id FOREIGN KEY (type_id) " +
								"REFERENCES vertex (id) ON DELETE CASCADE");
					query.executeUpdate();
					this.entityManager.getTransaction().commit();

					this.entityManager.getTransaction().begin();
					try {
						query = this.entityManager.createNativeQuery("create table schema_version (version int)");
						query.executeUpdate();
					} catch (Exception ignore) {
						this.entityManager.getTransaction().rollback();						
					}
					if (this.entityManager.getTransaction().isActive()) {
						this.entityManager.getTransaction().commit();						
					}
					this.entityManager.getTransaction().begin();
					
					query = this.entityManager.createNativeQuery("delete from schema_version");
					query.executeUpdate();
					query = this.entityManager.createNativeQuery("insert into schema_version (version) values (2)");
					query.executeUpdate();
					
					this.entityManager.getTransaction().commit();
				} catch (Exception failed) {
					this.bot.log(this, failed.toString(), Level.WARNING);
				} finally {
					try {
						if (this.entityManager.getTransaction().isActive()) {
							this.entityManager.getTransaction().rollback();
						}
					} catch (Exception failed2) {
						this.bot.log(this, failed2);					
					}				
				}
				this.entityManager.getTransaction().begin();
				try {
					Query query = this.entityManager.createNativeQuery("DELETE FROM relationship");
					query.executeUpdate();
					query = this.entityManager.createNativeQuery("DELETE FROM vertex");
					query.executeUpdate();
					query = this.entityManager.createNativeQuery("DELETE FROM imagedata");
					query.executeUpdate();
					
					this.entityManager.getTransaction().commit();
				} catch (Exception failed) {
					this.bot.log(this, failed.toString(), Level.WARNING);
				} finally {
					try {
						if (this.entityManager.getTransaction().isActive()) {
							this.entityManager.getTransaction().rollback();
						}
					} catch (Exception failed2) {
						this.bot.log(this, failed2);					
					}				
				}
			}
			
			//this.entityManager.unwrap(JpaEntityManager.class).getServerSession().getLogin().setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
			//this.entityManager.unwrap(JpaEntityManager.class).getServerSession().logout();
			//this.entityManager.unwrap(JpaEntityManager.class).getServerSession().login();
			this.longTermMemory = new DatabaseReadOnlyNetwork(getFactory().createEntityManager(), false);
			this.longTermMemory.setBot(this.bot);
			this.shortTermMemory = new DatabaseNetwork(this.entityManager, true);
			this.shortTermMemory.setBot(this.bot);
			
			this.listener = new LogListener() {
				/**
				 * Notify a logging level change.
				 */
				@Override
				public void logLevelChange(Level level) {
					Level debugLevel = bot.getDebugLevel();
					int logLevel = SessionLog.INFO;
					if (debugLevel == Level.ALL) {
						logLevel = SessionLog.FINEST;
					} else if (debugLevel == Level.FINEST) {
						logLevel = SessionLog.FINE;
					} else if (debugLevel == Level.FINE) {
						logLevel = SessionLog.INFO;
					} else if (debugLevel == Level.SEVERE) {
						logLevel = SessionLog.SEVERE;
					} else if (debugLevel == Level.WARNING) {
						logLevel = SessionLog.WARNING;
					} else if (debugLevel == Level.OFF) {
						logLevel = SessionLog.OFF;
					}
					entityManager.unwrap(JpaEntityManager.class).getServerSession().setLogLevel(logLevel);
				}
				
				@Override
				public void log(Object source, String message, Level level, Object[] arguments) {
				}
	
				@Override
				public void log(Throwable error) {			
				}
			};
			
			this.bot.addLogListener(this.listener);
			
			if (!recreateDatabase) {
				checkSchemaVersion();
			}
			
		} catch (RuntimeException failed) {
			this.bot.log(this, failed);
			throw failed;
		}
	}
	
	/**
	 * Create the database.
	 */
	public void createMemoryFromTemplate(String database, boolean isSchema, String template, boolean templateIsSchema) {
		Accessor accessor = null;
		Statement statement = null;
		try {
			accessor = ((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().acquireConnection();
			Connection connection = accessor.getConnection();
			statement = connection.createStatement();
			if (isSchema) {
				statement.executeUpdate("CREATE SCHEMA " + database);
				statement.close();
				((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().releaseConnection(accessor);
				shutdown();
				restore(database, true, true);

				accessor = ((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().acquireConnection();
				connection = accessor.getConnection();
				statement = connection.createStatement();
				if (templateIsSchema) {
					statement.executeUpdate("INSERT INTO VERTEX (SELECT * FROM " + template + ".VERTEX)");
					statement.executeUpdate("INSERT INTO RELATIONSHIP (SELECT * FROM " + template + ".RELATIONSHIP)");
					statement.executeUpdate("INSERT INTO TEXTDATA (SELECT * FROM " + template + ".TEXTDATA)");
					statement.executeUpdate("INSERT INTO IMAGEDATA (SELECT * FROM " + template + ".IMAGEDATA)");
					statement.executeUpdate("INSERT INTO PROPERTY (SELECT * FROM " + template + ".PROPERTY)");
					statement.executeUpdate("delete from SEQUENCE");
					statement.executeUpdate("INSERT INTO SEQUENCE (SELECT * FROM " + template + ".SEQUENCE)");
				} else {
					final Properties properties = new Properties();
					DatasourceLogin login = (DatasourceLogin)((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().getLogin();
					Connector connector = new Connector() {
						public Connection connect(Properties p, Session session) {
							properties.put("password", p.get("password"));
							return null;
						}						
						public Object clone() { return null; }						
						public void toString(PrintWriter writer) {}						
						public String getConnectionDetails() { return null; }
					};
					Connector oldConnector = login.getConnector();
					try {
						login.setConnector(connector);
						login.connectToDatasource(null, null);
					} finally {
						login.setConnector(oldConnector);
					}
					String pw = (String)properties.get("password");
					try {
						statement.executeUpdate("CREATE EXTENSION dblink schema public");
					} catch (Exception alreadyExists) { }
					statement.execute("select public.dblink_connect('dbconnection','dbname=" + template + " user=postgres password=" + pw + "')");
					statement.executeUpdate(
							"INSERT INTO VERTEX (id, accesscount, accessdate, creationdate, datatype, datavalue, dirty, groupid, hasresponse, name, pinned, wordcount) (SELECT id, accesscount, accessdate, creationdate, datatype, datavalue, dirty, groupid, hasresponse, name, pinned, wordcount FROM public.dblink('dbconnection', 'SELECT id, accesscount, accessdate, creationdate, datatype, datavalue, dirty, groupid, hasresponse, name, pinned, wordcount FROM VERTEX') AS T1(id bigint, accesscount integer, accessdate timestamp, creationdate timestamp, datatype varchar, datavalue varchar, dirty boolean, groupid bigint, hasresponse boolean, name varchar, pinned boolean, wordcount integer))");
					statement.executeUpdate(
							"INSERT INTO RELATIONSHIP (id, accesscount, accessdate, correctness, creationdate, hashcode, source_index, pinned, meta_id, source_id, target_id, type_id) (SELECT id, accesscount, accessdate, correctness, creationdate, hashcode, source_index, pinned, meta_id, source_id, target_id, type_id FROM public.dblink('dbconnection', 'SELECT id, accesscount, accessdate, correctness, creationdate, hashcode, source_index, pinned, meta_id, source_id, target_id, type_id FROM RELATIONSHIP') AS T1(id bigint, accesscount integer, accessdate timestamp, correctness double precision, creationdate timestamp, hashcode integer, source_index integer, pinned boolean, meta_id bigint, source_id bigint, target_id bigint, type_id bigint))");
					statement.executeUpdate(
							"INSERT INTO TEXTDATA (id, text_data) (SELECT id, text_data FROM public.dblink('dbconnection', 'SELECT id, text_data FROM TEXTDATA') AS T1(id bigint, text_data text))");
					statement.executeUpdate(
							"INSERT INTO IMAGEDATA (id, image_data) (SELECT id, image_data FROM public.dblink('dbconnection', 'SELECT id, image_data FROM IMAGEDATA') AS T1(id bigint, image_data bytea))");					
					statement.executeUpdate(
							"INSERT INTO PROPERTY (property, value, startup) (SELECT property, value, startup FROM public.dblink('dbconnection', 'SELECT property, value, startup FROM PROPERTY') AS T1(property varchar, value varchar, startup boolean))");
					statement.executeUpdate("delete from SEQUENCE");					
					statement.executeUpdate(
							"INSERT INTO SEQUENCE (seq_name, seq_count) (SELECT seq_name, seq_count FROM public.dblink('dbconnection', 'SELECT seq_name, seq_count FROM SEQUENCE') AS T1(seq_name varchar, seq_count numeric))");
					statement.execute("select public.dblink_disconnect('dbconnection')");
				}
				statement.close();
				((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().releaseConnection(accessor);
			} else {
				statement.executeUpdate("CREATE DATABASE " + database + " WITH TEMPLATE " + template);
				statement.close();
				((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().releaseConnection(accessor);
			}
		} catch (Exception failed) {
			this.bot.log(this, failed);
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception ignore) {}
			}
			if (accessor != null) {
				try {
					((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().releaseConnection(accessor);
				} catch (Exception ignore) {}
			}
			throw new RuntimeException(failed);
		}
	}

	/**
	 * Drop the database.
	 */
	@Override
	public void destroyMemory(String database, boolean isSchema) {
		try {
			Accessor accessor = ((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().acquireConnection();
			Connection connection = accessor.getConnection();
			Statement statement = connection.createStatement();
			if (isSchema) {
				statement.executeUpdate("DROP SCHEMA " + database + " CASCADE");				
			} else {
				statement.executeUpdate("DROP DATABASE " + database);
			}
			statement.close();
			((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().releaseConnection(accessor);
		} catch (Exception failed) {
			this.bot.log(this, failed);
			throw new RuntimeException(failed);
		}
	}

}

