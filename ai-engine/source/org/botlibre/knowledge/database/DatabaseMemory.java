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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.metamodel.Metamodel;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl;
import org.eclipse.persistence.internal.jpa.metadata.xml.XMLEntityMappingsReader;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.jpa.JpaEntityManagerFactory;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.server.ConnectionPolicy;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.botlibre.LogListener;
import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.BasicMemory;
import org.botlibre.knowledge.BasicNetwork;
import org.botlibre.knowledge.BasicVertex;
import org.botlibre.util.Utils;

/**
 * Uses JPA to store the memory to a Derby database.
 */

public class DatabaseMemory extends BasicMemory {
	
	/* HSQL
	public static String DATABASE_URL = "jdbc:hsqldb:file:Bot01";
	public static String DATABASE_DRIVER = "org.hsqldb.jdbc.JDBCDriver";
	public static String DATABASE_USER = "SA";*/
	/* Derby
	public static String DATABASE_URL = "jdbc:derby:Bot;create=true";
	public static String DATABASE_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";*/
	/* MySQL
	public static String DATABASE_USER = "root";
	public static String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
	public static String DATABASE_URL = "jdbc:mysql://localhost/Bot?createDatabaseIfNotExist=true";*/
	/* PostgreSQL */
	public static String DATABASE_USER = "postgres";
	public static String DATABASE_PASSWORD = "password";
	public static String IMPORT_URL = "jdbc:postgresql:";
	public static String DATABASE_URL = "jdbc:postgresql:postgres";
	public static String DATABASE_TEST_URL = "jdbc:postgresql:test";
	public static String DATABASE_DRIVER = "org.postgresql.Driver";
	public static String CACHE_SIZE = "5000";
	public static boolean TEST = true;
	public static boolean RECREATE_DATABASE = false;
	
	public static ConcurrentMap<String, SessionInfo> sessions = new ConcurrentHashMap<String, SessionInfo>();
	
	private EntityManagerFactory factory;
	private EntityManager entityManager;
	private LogListener listener;
	private String database;
	private boolean isFast;
	
	public class SessionInfo {
		public ServerSession session;
		public int count;
		
		public int increment() {
			count++;
			return count;
		}
		
		public int decrement() {
			count--;
			return count;
		}
	}
	
	public DatabaseMemory() {
		super();
		if (TEST) {
			this.database = DATABASE_TEST_URL.substring(IMPORT_URL.length());
		} else {
			this.database = DATABASE_URL.substring(IMPORT_URL.length());
		}
	}
	
	/**
	 * Initialize any configurable settings from the properties.
	 */
	@Override
	public void initialize(Map<String, Object> properties) {
		if (properties.containsKey("jdbc.user")) {
			DATABASE_USER = (String)properties.get("jdbc.user");
			this.bot.log(this, "Init property:", Level.FINEST, "jdbc.user", DATABASE_USER);
		}
		if (properties.containsKey("jdbc.password")) {
			DATABASE_PASSWORD = (String)properties.get("jdbc.password");
			this.bot.log(this, "Init property:", Level.FINEST, "jdbc.password", DATABASE_PASSWORD);
		}
		if (properties.containsKey("jdbc.url")) {
			DATABASE_URL = (String)properties.get("jdbc.url");
			this.bot.log(this, "Init property:", Level.FINEST, "jdbc.url", DATABASE_URL);
		}
		if (properties.containsKey("jdbc.driver")) {
			DATABASE_DRIVER = (String)properties.get("jdbc.driver");
			this.bot.log(this, "Init property:", Level.FINEST, "jdbc.driver", DATABASE_DRIVER);
		}
		if (properties.containsKey("cache.size")) {
			CACHE_SIZE = (String)properties.get("cache.size");
			BasicNetwork.MAX_SIZE = Integer.parseInt((String)properties.get("cache.size"));
			this.bot.log(this, "Init property:", Level.FINEST, "cache.size", CACHE_SIZE);
		}
	}
	
	public EntityManagerFactory getFactory() {
		return factory;
	}

	public void setFactory(EntityManagerFactory factory) {
		this.factory = factory;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Commit short-term memory to the database.
	 */
	@Override
	public synchronized void save() {
		getShortTermMemory().save();
		// Check for low memory.
		if (Utils.checkLowMemory()) {
			this.bot.log(this, "Low memory - clearing short term memory", Level.INFO);
			getShortTermMemory().clear();
			if (cacheSize() > Bot.MAX_CACHE) {
				this.bot.log(this, "Cache too big - clearing server cache", Level.WARNING, cacheSize(), Bot.MAX_CACHE);
				freeMemory();
			}
		} else {
			getShortTermMemory().resume();
		}
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	public void pool() {
		if (getShortTermMemory() != null) {
			getShortTermMemory().clear();
		}		
	}
		
	/**
	 * Shutdown the database.
	 */
	@Override
	public synchronized void shutdown() {
		try {
			if (getShortTermMemory() != null) {
				getShortTermMemory().clear();
			}
			ServerSession session = null;
			if (this.entityManager != null) {
				session = this.entityManager.unwrap(ServerSession.class);
				save();
				this.entityManager.clear();
				this.entityManager.close();
			}
			if (getFactory() != null) {
				getFactory().close();
				if (this.isFast && session != null) {
					SessionInfo info = sessions.get(this.database);
					if (info != null) {
						synchronized (sessions) {
							info.decrement();
							if (info.count <= 0) {
								sessions.remove(this.database);
							}
						}
						synchronized (info) {
							if (info.count <= 0) {
								if (session.isLoggedIn()) {
									session.logout();
								}
								// Should be the same.
								if (info.session.isLoggedIn()) {
									info.session.logout();
								}
								info.session = null;
							}
						}
					} else {
						// Should never happen.
						if (session.isLoggedIn()) {
							session.logout();
						}
					}
				}
			}
			this.bot.removeLogListener(this.listener);
			setEntityManager(null);
			setFactory(null);
		} catch (Exception exception) {
			this.bot.log(this, exception);
		}
	}

	/**
	 * This implementation does not support persistence.
	 */
	@Override
	public void restore() {
		restore("");
	}
	
	/**
	 * Connect and create the EntityManager.
	 */
	@Override
	public void restore(String database) {
		restore(database, false);
	}
	
	public void checkSchemaVersion() {
		boolean schemaMigrationRequired = true;
		// Check database version and migrate schema if required.
		try {
			int version = ((Number)this.entityManager.createNativeQuery("select version from schema_version").getSingleResult()).intValue();
			if (version == 1) {
				schemaMigrationRequired = false;
			}
		} catch (Exception missing) {
			executeDDL("create table schema_version (version int)");
			executeDDL("insert into schema_version (version) values (1)");
		}
		if (schemaMigrationRequired) {
			this.bot.log(this, "Migrating schema", Level.WARNING);
			executeDDL("update vertex set datavalue = replace(datavalue, 'org.pandora', 'org.botlibre') where datavalue like 'org.pandora.%'");
			try {
				this.entityManager.createNativeQuery("select wordcount from vertex where wordcount <> wordcount").getResultList();
			} catch (Exception missing) {
				executeDDL("alter table vertex ADD COLUMN wordcount int");
			}
			try {
				this.entityManager.createNativeQuery("select groupid from vertex where groupid <> groupid").getResultList();
			} catch (Exception missing) {
				executeDDL("alter table vertex ADD COLUMN groupid int");
			}
			try {
				this.entityManager.createNativeQuery("select hashcode from relationship where hashcode <> hashcode").getResultList();
			} catch (Exception missing) {
				executeDDL("alter table relationship ADD COLUMN hashcode int");
			}
			try {
				this.entityManager.createNativeQuery("select id from textdata where id <> id").getResultList();
			} catch (Exception missing) {
				executeDDL("CREATE TABLE TEXTDATA (ID BIGINT NOT NULL, TEXT_DATA TEXT, PRIMARY KEY (ID))");
			}
			/*executeDDL("alter table relationship alter type_id set not null");
			executeDDL("alter table relationship alter target_id set not null");
			executeDDL("alter table relationship alter source_id set not null");
			executeDDL("ALTER TABLE vertex ADD COLUMN groupid bigint");
			executeDDL("ALTER TABLE relationship ADD COLUMN hashcode integer");*/
		}
	}
	
	/**
	 * Connect and create the EntityManager.
	 */
	public void restore(String database, boolean recreateDatabase) {
		try {
			if (getFactory() != null) {
				getFactory().close();
			}
			this.bot.log(this, "Restoring", Bot.FINE);
			Map<String, String> properties = new HashMap<String, String>();
			//properties.put(PersistenceUnitProperties.JDBC_DRIVER, DATABASE_DRIVER);
			if (database.equals("")) {
				if (TEST) {
					properties.put(PersistenceUnitProperties.JDBC_URL, DATABASE_TEST_URL);
					this.database = DATABASE_TEST_URL.substring(IMPORT_URL.length());
				} else {
					properties.put(PersistenceUnitProperties.JDBC_URL, DATABASE_URL);
					this.database = DATABASE_URL.substring(IMPORT_URL.length());
				}
			} else {
				properties.put(PersistenceUnitProperties.JDBC_URL, IMPORT_URL + database);
				this.database = database;
			}
			//properties.put(PersistenceUnitProperties.JDBC_USER, DATABASE_USER);
			//properties.put(PersistenceUnitProperties.JDBC_PASSWORD, DATABASE_PASSWORD);
			properties.put(PersistenceUnitProperties.CACHE_SIZE_DEFAULT, CACHE_SIZE);
			properties.put(PersistenceUnitProperties.CACHE_STATEMENTS, "true");
			properties.put(PersistenceUnitProperties.BATCH_WRITING, "JDBC");
			if (recreateDatabase || (TEST && RECREATE_DATABASE)) {
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
			checkSchemaVersion();
			
			//this.entityManager.unwrap(JpaEntityManager.class).getServerSession().getLogin().setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
			//this.entityManager.unwrap(JpaEntityManager.class).getServerSession().logout();
			//this.entityManager.unwrap(JpaEntityManager.class).getServerSession().login();
			this.longTermMemory = new DatabaseNetwork(this.entityManager, false);
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
			
		} catch (RuntimeException failed) {
			this.bot.log(this, failed);
			throw failed;
		}
	}
	
	public static void forceShutdown(String name) {
		SessionInfo info = sessions.remove(name);
		if (info != null) {
			new Bot().log(info.session, "Forced shutdown", Level.WARNING);
			if (info.session != null) {
				info.session.logout();
			}
		}
	}
	
	/**
	 * Connect and create the EntityManager.
	 */
	public void fastRestore(String database) {
		try {
			Bot cache = Bot.getSystemCache();
			if (cache == null) {
				restore(database);
				return;
			}
			if (getFactory() != null) {
				getFactory().close();
			}
			this.bot.log(this, "Fast restoring", Level.FINE);

			DatabaseMemory cacheMemory = (DatabaseMemory)cache.memory();
			Metamodel metaModel = ((EntityManagerFactoryImpl)cacheMemory.getFactory()).getMetamodel();
			SessionInfo info = null;
			synchronized (sessions) {
				info = sessions.get(database);
				if (info == null) {
					info = new SessionInfo();
					info.increment();
					sessions.put(database, info);
				} else {
					info.increment();
				}
			}
			ServerSession session = null;
			synchronized (info) {
				session = info.session;
				if (session == null) {
					ServerSession cacheSession = cacheMemory.getEntityManager().unwrap(ServerSession.class);
					Project project = cacheSession.getProject().clone();
					DatabaseLogin login = (DatabaseLogin)cacheSession.getLogin().clone();
					login.setURL(IMPORT_URL + database);
					project.setLogin(login);
					
					session = new ServerSession(project, new ConnectionPolicy(ServerSession.DEFAULT_POOL), 1, 32, 32, login, login);
					session.setSessionLog((SessionLog)cacheSession.getSessionLog().clone());
					session.login();
					info.session = session;
				}
			}
			
			EntityManagerFactoryImpl factory = new EntityManagerFactoryImpl(session);
			try {
				EntityManagerSetupImpl setupImpl = new EntityManagerSetupImpl();
				setupImpl.setMetamodel(metaModel);
				Field field = EntityManagerFactoryImpl.class.getDeclaredField("delegate");
				field.setAccessible(true);
				EntityManagerFactoryDelegate delegate = (EntityManagerFactoryDelegate)field.get(factory);
				field =  EntityManagerFactoryDelegate.class.getDeclaredField("setupImpl");
				field.setAccessible(true);
				field.set(delegate, setupImpl);
			} catch (Exception exception) {
				this.bot.log(this, exception);
				throw new BotException(exception);				
			}
			factory.setCommitOrder(UnitOfWork.CommitOrderType.ID);
			factory.setFlushMode(FlushModeType.COMMIT);
			
			setFactory(factory);
			setEntityManager(getFactory().createEntityManager());

			checkSchemaVersion();

			this.database = database;
			this.isFast = true;
			this.longTermMemory = new DatabaseNetwork(this.entityManager, false);
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
			
		} catch (RuntimeException failed) {
			this.bot.log(this, failed);
			throw failed;
		}
	}

	public boolean executeDDL(String ddl) {
		this.entityManager.getTransaction().begin();
		try {
			Query query = this.entityManager.createNativeQuery(ddl);
			query.executeUpdate();
			this.entityManager.getTransaction().commit();
			return true;
		} catch (Exception failed) {
			//this.bot.log(this, failed.toString(), Level.WARNING);
		} finally {
			try {
				if (this.entityManager.getTransaction().isActive()) {
					this.entityManager.getTransaction().rollback();
				}
			} catch (Exception failed2) {
				this.bot.log(this, failed2);					
			}
		}
		return false;
	}
	
	/**
	 * Return an isolated transactional memory.
	 * Can be used by senses or sub-conscious thought for concurrent processing.
	 */
	@Override
	public Network newMemory() {
		DatabaseNetwork memory = new DatabaseNetwork(getFactory().createEntityManager(), true);
		memory.setBot(this.bot);
		return memory;
	}

	/**
	 * Return the current connected database name.
	 */
	@Override
	public String getMemoryName() {
		return database;
	}
	
	/**
	 * Create the database.
	 */
	@Override
	public void createMemory(String database) {
		try {
			Accessor accessor = ((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().acquireConnection();
			Connection connection = accessor.getConnection();
			connection.createStatement().executeUpdate("CREATE DATABASE " + database);
			((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().releaseConnection(accessor);
		} catch (Exception failed) {
			this.bot.log(this, failed);
			throw new RuntimeException(failed);
		}
	}
	
	/**
	 * Create the database.
	 */
	@Override
	public void createMemoryFromTemplate(String database, String template) {
		try {
			Accessor accessor = ((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().acquireConnection();
			Connection connection = accessor.getConnection();
			connection.createStatement().executeUpdate("CREATE DATABASE " + database + " WITH TEMPLATE " + template);
			((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().releaseConnection(accessor);
		} catch (Exception failed) {
			this.bot.log(this, failed);
			throw new RuntimeException(failed);
		}
	}	

	/**
	 * Drop the database.
	 */
	@Override
	public void destroyMemory(String database) {
		if (!TEST) {
			throw new BotException("Can only destroy instance when in test mode.");
		}
		try {
			Accessor accessor = ((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().acquireConnection();
			Connection connection = accessor.getConnection();
			connection.createStatement().executeUpdate("DROP DATABASE " + database);
			((JpaEntityManagerFactory)getFactory()).getServerSession().getReadConnectionPool().releaseConnection(accessor);
		} catch (Exception failed) {
			this.bot.log(this, failed);
			throw new RuntimeException(failed);
		}
	}

	/**
	 * Delete all content from the database.
	 */
	@Override
	public void deleteMemory() {
		if (!TEST) {
			throw new BotException("Can only delete instance when in test mode.");
		}
		restore(getMemoryName(), true);
	}

	/**
	 * Switch to a different database.
	 */
	@Override
	public void switchMemory(String database) {
		RECREATE_DATABASE = false;
		shutdown();
		restore(database);
	}
	
	/**
	 * Import the database into this instance.
	 */
	@SuppressWarnings("unchecked")
	public void importMemory(String database) {
		try {
			Map<String, String> properties = new HashMap<String, String>();
			//properties.put(PersistenceUnitProperties.JDBC_DRIVER, DATABASE_DRIVER);
			properties.put(PersistenceUnitProperties.JDBC_URL, IMPORT_URL + database);
			//properties.put(PersistenceUnitProperties.JDBC_USER, DATABASE_USER);
			//properties.put(PersistenceUnitProperties.JDBC_PASSWORD, DATABASE_PASSWORD);
			Level debugLevel = this.bot.getDebugLevel();
			String logLevel = "INFO";
			if (debugLevel == Level.FINEST) {
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
			EntityManagerFactory importFactory = Persistence.createEntityManagerFactory("import", properties);
			EntityManager importEntityManager = importFactory.createEntityManager();
			Query query = importEntityManager.createQuery("Select v from Vertex v order by v.id");
			int start = 0;
			query.setFirstResult(start);
			query.setMaxResults(100);
			List<Vertex> vertices = query.getResultList();
			Map<Vertex, Vertex> identitySet = new IdentityHashMap<Vertex, Vertex>(vertices.size());
			while (!vertices.isEmpty()) {
				for (Vertex vertex : vertices) {
					getShortTermMemory().importMerge(vertex, identitySet);
				}
				save();
				start = start + 100;
				query.setFirstResult(start);
				query.setMaxResults(100);
				vertices = query.getResultList();
			}
			importFactory.close();
		} catch (RuntimeException failed) {
			this.bot.log(this, failed);
			throw failed;
		}
	}

	/**
	 * Clear the memory.
	 */
	@Override
	public void abort() {
		super.abort();
		this.entityManager.clear();
	}
	
	@Override
	public int cacheSize() {
		return this.entityManager.unwrap(ServerSession.class).getIdentityMapAccessorInstance().getIdentityMap(BasicVertex.class).getSize();
	}
	
	@Override
	public void freeMemory() {
		this.entityManager.unwrap(ServerSession.class).getIdentityMapAccessor().initializeAllIdentityMaps();
	}

}

