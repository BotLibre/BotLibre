/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.admin;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.knowledge.BasicRelationship;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.sense.text.TextInput;
import org.botlibre.util.Utils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.jpa.JpaEntityManagerFactory;

import org.botlibre.web.Site;
import org.botlibre.web.admin.User.UserType;
import org.botlibre.web.service.BotManager;
import org.botlibre.web.service.Stats;

public class Migrate  {
	public static void main(String[] args) {
		new Migrate().migrate();
		System.exit(0);
	}

	public Bot connectInstance(BotInstance instance) {
		Bot bot = Bot.createInstanceFromPool(instance.getDatabaseName(), instance.isSchema());
		bot.setName(instance.getName());
		if (instance.isAdult()) {
			bot.setFilterProfanity(false);
		}
		return bot;
	}

	public void initDatabase() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_URL, Site.getDatabaseUrl() + "postgres");
		properties.put(PersistenceUnitProperties.JDBC_USER, Site.DATABASEUSER);
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
		//properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
		EntityManagerFactory postgres = Persistence.createEntityManagerFactory("postgres", properties);
		EntityManagerFactory factory = null;
		Accessor accessor = null;
		Statement statement = null;
		try {
			AdminDatabase.instance().log(Level.INFO, "Creating database");
			try {
				accessor = ((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().acquireConnection();
				Connection connection = accessor.getConnection();
				statement = connection.createStatement();
				try {
					connection.createStatement().executeUpdate("CREATE DATABASE " + Site.PERSISTENCE_UNIT);
				} catch (Exception exception) {
					AdminDatabase.instance().log(exception);
				}
				try {
					connection.createStatement().executeUpdate("CREATE DATABASE " + Site.PERSISTENCE_UNIT + "_bots");
				} catch (Exception exception) {
					AdminDatabase.instance().log(exception);
				}
				((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().releaseConnection(accessor);
				
				properties = new HashMap<String, String>();
				properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
				properties.put(PersistenceUnitProperties.JDBC_URL, DatabaseMemory.DATABASE_URL);
				properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
				factory = Persistence.createEntityManagerFactory("Bot", properties);
				
				accessor = ((JpaEntityManagerFactory)factory).getServerSession().getReadConnectionPool().acquireConnection();
				connection = accessor.getConnection();
				statement = connection.createStatement();
				try {
					statement.executeUpdate("CREATE EXTENSION dblink schema public");
				} catch (Exception exception) {
					AdminDatabase.instance().log(exception);
				}
				((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().releaseConnection(accessor);
				
				factory.createEntityManager().close();
				factory.close();
				
			} catch (Exception exception) {
				AdminDatabase.instance().log(exception);
			}
			
			postgres.close();
			AdminDatabase.instance().log(Level.INFO, "Init complete");
		} catch (RuntimeException exception) {
			if (statement != null) {
				try {
					statement.close();
				} catch (Exception ignore) {}
			}
			if (accessor != null) {
				try {
					((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().releaseConnection(accessor);
				} catch (Exception ignore) {}
			}
			if (factory != null) {
				try {
					factory.close();
				} catch (Exception ignore) {}
			}
			if (postgres != null) {
				try {
					postgres.close();
				} catch (Exception ignore) {}
			}
			AdminDatabase.instance().log(exception);
			throw exception;
		}
	}

	@SuppressWarnings("unchecked")
	public void migrate() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
		//properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT, properties);
		EntityManager em = factory.createEntityManager();
		List<BotInstance> instances = em.createQuery("Select p from BotInstance p").getResultList();
		AdminDatabase.instance().log(Level.INFO, "Migrating", instances.size());
		int count = 0;
		for (BotInstance instance : instances) {
			if (instance.isExternal() || instance.isArchived()) {
				continue;
			}
			if (instance.isSchema()) {
				continue;
			}
			count++;
			try {
				// Sleep when busy.
				while ((System.currentTimeMillis() - Stats.lastChat) < 10000) {
					Utils.sleep(5000);
				}
				AdminDatabase.instance().log(Level.INFO, "Migrating", count, instance.getName());
				try {
					Bot bot = connectInstance(instance);
					Network memory = bot.memory().newMemory();
					memory.executeNativeQuery("update relationship r set meta_id = null where r.meta_id is not null and r.meta_id not in (select v.id from vertex v)");
					bot.shutdown();

					BotManager.manager().forceShutdown(instance.getDatabaseName());
					Utils.sleep(1000);
					Bot.forceShutdown(instance.getDatabaseName());

					bot = Bot.createInstance();
					bot.memory().createMemoryFromTemplate(instance.getDatabaseName(), true, instance.getDatabaseName(), false);
					bot.memory().switchMemory(instance.getDatabaseName(), true);
					bot.shutdown();
					
					AdminDatabase.instance().updateInstanceSchema(instance.getId(), true);

					BotManager.manager().forceShutdown(instance.getDatabaseName());
					Utils.sleep(1000);
					Bot.forceShutdown(instance.getDatabaseName());
					bot = Bot.createInstance();
					try {
						bot.memory().destroyMemory(instance.getDatabaseName(), false);
					} finally {
						bot.shutdown();
					}
				} catch (Exception failed) {
					AdminDatabase.instance().log(failed);
				}
				
				Utils.sleep(2000);
				AdminDatabase.instance().log(Level.INFO, "Migration complete", count, instance.getName());
				Thread.sleep(2000);
			} catch (Exception exception) {
				AdminDatabase.instance().log(exception);
			}
		}
		AdminDatabase.instance().log(Level.INFO, "Migration complete", count);		
	}

	@SuppressWarnings("unchecked")
	public void migrate6() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
		//properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT, properties);
		EntityManager em = factory.createEntityManager();
		List<BotInstance> instances = em.createQuery("Select p from BotInstance p").getResultList();
		AdminDatabase.instance().log(Level.INFO, "Migrating", instances.size());
		int count = 0;
		for (BotInstance instance : instances) {
			if (instance.isExternal() || instance.isArchived()) {
				continue;
			}
			count++;
			try {
				AdminDatabase.instance().log(Level.INFO, "Migrating", count, instance.getName());
				Bot bot = connectInstance(instance);
				Utils.sleep(5000);
				bot.shutdown();
				AdminDatabase.instance().log(Level.INFO, "Migration complete", count, instance.getName());
				Thread.sleep(5000);
			} catch (Exception exception) {
				AdminDatabase.instance().log(exception);
			}
		}
		AdminDatabase.instance().log(Level.INFO, "Migration complete", count);		
	}

	@SuppressWarnings("unchecked")
	public void migrate5() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
		//properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT, properties);
		EntityManager em = factory.createEntityManager();
		List<BotInstance> instances = em.createQuery("Select p from BotInstance p").getResultList();
		AdminDatabase.instance().log(Level.INFO, "Migrating", instances.size());
		int count = 0;
		for (BotInstance instance : instances) {
			count++;
			try {
				AdminDatabase.instance().log(Level.INFO, "Migrating", count, instance.getName());
				Bot bot = connectInstance(instance);

				EntityManager db = ((DatabaseMemory)bot.memory()).getEntityManager();
				try {
					db.getTransaction().begin();
					Query query = db.createNativeQuery("update vertex set hasresponse = null");
					query.executeUpdate();
					db.getTransaction().commit();
				} catch (Exception exception) {
					AdminDatabase.instance().log(exception);
				} finally {
					try {
						if (db.getTransaction().isActive()) {
							db.getTransaction().rollback();
						}
					} catch (Exception exception) {
						AdminDatabase.instance().log(exception);
					}
				}
				
				List<BasicRelationship> results = null;
				int total = 0;
				while (((results == null) || (results.size() > 0)) && (total < 100)) {
					total++;
					Network memory = bot.memory().newMemory();
					results = memory.findByNativeQuery(
							"SELECT * FROM RELATIONSHIP WHERE HASHCODE IS NULL LIMIT 5000",
							BasicRelationship.class, 5000);
					for (BasicRelationship relationship : results) {
						relationship.hashCode();
					}
					memory.save();
					Thread.sleep(1000);
				}				
				bot.shutdown();
				AdminDatabase.instance().log(Level.INFO, "Migration complete", count, instance.getName());
				Thread.sleep(5000);
			} catch (Exception exception) {
				AdminDatabase.instance().log(exception);
			}
		}
		AdminDatabase.instance().log(Level.INFO, "Migration complete", count);		
	}

	public void migrate7() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "fine");
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT, properties);
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		try {
			em.createNativeQuery("Update ChatChannel set alias = name").executeUpdate();
			em.createNativeQuery("Update Forum set alias = name").executeUpdate();
			em.createNativeQuery("Update BotInstance set alias = name").executeUpdate();
			em.createNativeQuery("Update Graphic set alias = name").executeUpdate();
			em.createNativeQuery("Update Domain set alias = name").executeUpdate();
			em.createNativeQuery("Update IssueTracker set alias = name").executeUpdate();
			em.createNativeQuery("Update Analytic set alias = name").executeUpdate();
			em.createNativeQuery("Update Script set alias = name").executeUpdate();
			em.createNativeQuery("Update Avatar set alias = name").executeUpdate();
			em.getTransaction().commit();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
			factory.close();
		}
	}

	@SuppressWarnings("unchecked")
	public void dropDead() {
		try {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
			//properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
			EntityManagerFactory postgres = Persistence.createEntityManagerFactory("postgres", properties);
			EntityManager pg = postgres.createEntityManager();
			Set<String> databases = new HashSet<String>(pg.createNativeQuery("SELECT datname FROM pg_database WHERE datistemplate = false and datname like '" + Site.PERSISTENCE_UNIT + "_%'").getResultList());
			databases.remove(Site.PERSISTENCE_UNIT + "_template");
			databases.remove(Site.PERSISTENCE_UNIT + "_migration");
			databases.remove(Site.PERSISTENCE_UNIT + "_bots");
			
			EntityManagerFactory factory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT, properties);
			EntityManager em = factory.createEntityManager();
			List<BotInstance> instances = em.createQuery("Select p from BotInstance p").getResultList(); // Must be all
			AdminDatabase.instance().log(Level.INFO, "Dropping dead instances", instances.size());
			int count = 0;
			Set<String> dbnames = new HashSet<String>();
			em.getTransaction().begin();
			for (BotInstance instance : instances) {
				try {
					dbnames.add(instance.getDatabaseName());
					if (!instance.isExternal() && !instance.isSchema() && !instance.isArchived() && !databases.contains(instance.getDatabaseName())) {
						count++;
						AdminDatabase.instance().log(Level.INFO, "Dropping dead instance", instance);
						instance.preDelete(em);
						em.remove(instance);
						Utils.sleep(1000);
					}
				} catch (Exception exception) {
					AdminDatabase.instance().log(exception);
				}
			}
			em.getTransaction().commit();
			for (String db : databases) {
				if (!dbnames.contains(db)) {
					count++;
					AdminDatabase.instance().log(Level.INFO, "Dropping dead database", db);
					try {
						Accessor accessor = ((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().acquireConnection();
						Connection connection = accessor.getConnection();
						connection.createStatement().executeUpdate("DROP DATABASE " + db);
						((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().releaseConnection(accessor);
						Utils.sleep(1000);
					} catch (Exception exception) {
						AdminDatabase.instance().log(exception);
					}
				}
				
			}
			em.close();
			factory.close();
			pg.close();
			postgres.close();
			AdminDatabase.instance().log(Level.INFO, "Dropping dead complete", count);
		} catch (RuntimeException exception) {
			AdminDatabase.instance().log(exception);
			throw exception;
		}
		
		dropDeadSchemas();
	}


	@SuppressWarnings("unchecked")
	public void dropDeadSchemas() {
		try {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(PersistenceUnitProperties.JDBC_URL, DatabaseMemory.DATABASE_URL);
			properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
			//properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
			EntityManagerFactory postgres = Persistence.createEntityManagerFactory("postgres", properties);
			EntityManager pg = postgres.createEntityManager();
			EntityManagerFactory botsFactory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT + "_bots", properties);
			botsFactory.createEntityManager().close();
			
			Set<String> schemas = new HashSet<String>(pg.createNativeQuery("select nspname from pg_catalog.pg_namespace where nspname like '" + Site.PERSISTENCE_UNIT + "_%'").getResultList());

			properties = new HashMap<String, String>();
			EntityManagerFactory factory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT, properties);
			EntityManager em = factory.createEntityManager();
			List<BotInstance> instances = em.createQuery("Select p from BotInstance p").getResultList(); // Must be all
			AdminDatabase.instance().log(Level.INFO, "Dropping dead schema instances", instances.size());
			int count = 0;
			Set<String> dbnames = new HashSet<String>();
			em.getTransaction().begin();
			for (BotInstance instance : instances) {
				try {
					dbnames.add(instance.getDatabaseName());
					if (!instance.isExternal() && instance.isSchema() && !instance.isArchived() && !schemas.contains(instance.getDatabaseName())) {
						count++;
						AdminDatabase.instance().log(Level.INFO, "Dropping dead instance", instance);
						instance.preDelete(em);
						em.remove(instance);
						Utils.sleep(1000);
					}
				} catch (Exception exception) {
					AdminDatabase.instance().log(exception);
				}
			}
			em.getTransaction().commit();
			for (String schema : schemas) {
				if (!dbnames.contains(schema)) {
					count++;
					AdminDatabase.instance().log(Level.INFO, "Dropping dead schema", schema);
					try {
						Accessor accessor = ((JpaEntityManagerFactory)botsFactory).getServerSession().getReadConnectionPool().acquireConnection();
						Connection connection = accessor.getConnection();
						Statement statement = connection.createStatement();
						statement.executeUpdate("DROP SCHEMA " + schema + " CASCADE");
						statement.close();
						((JpaEntityManagerFactory)botsFactory).getServerSession().getReadConnectionPool().releaseConnection(accessor);
					} catch (Exception exception) {
						AdminDatabase.instance().log(exception);
					}
					Utils.sleep(1000);
				}				
			}
			em.close();
			factory.close();
			pg.close();
			postgres.close();
			botsFactory.close();
			AdminDatabase.instance().log(Level.INFO, "Dropping dead schemas complete", count);
		} catch (RuntimeException exception) {
			AdminDatabase.instance().log(exception);
			throw exception;
		}
	}

	@SuppressWarnings("unchecked")
	public void archiveInactive() {
		EntityManagerFactory postgres = null;		
		EntityManagerFactory factory = null;
		EntityManager em = null;
		EntityManagerFactory botsFactory = null;
		try {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
			properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
			postgres = Persistence.createEntityManagerFactory("postgres", properties);
			botsFactory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT + "_bots", properties);
			botsFactory.createEntityManager().close();
			factory = Persistence.createEntityManagerFactory(Site.PERSISTENCE_UNIT, properties);
			em = factory.createEntityManager();
			Query query = em.createQuery("Select p from BotInstance p "
						+ "where p.archived = false and p.lastConnected < :date and p.isExternal = false and p.isTemplate = false and p.enableTwitter = false and p.enableFacebook = false and p.enableEmail = false and p.creator.type = :type");
			Calendar lastYear = Calendar.getInstance();
			lastYear.add(Calendar.MONTH, -6);
			query.setParameter("date", lastYear.getTime());
			query.setParameter("type", UserType.Basic);
			List<BotInstance> instances = query.getResultList();
			AdminDatabase.instance().log(Level.INFO, "Archiving inactive instances", instances.size());
			int count = 0;
			for (BotInstance instance : instances) {
				try {
					AdminDatabase.instance().log(Level.INFO, "Archiving inactive instance", instance);
					Utils.sleep(1000);
					count++;
					if (instance.isSchema()) {
						Accessor accessor = ((JpaEntityManagerFactory)botsFactory).getServerSession().getReadConnectionPool().acquireConnection();
						Connection connection = accessor.getConnection();
						Statement statement = connection.createStatement();
						try {
							statement.executeUpdate("DROP SCHEMA " + instance.getDatabaseName() + " CASCADE");
						} finally {
							statement.close();
							((JpaEntityManagerFactory)botsFactory).getServerSession().getReadConnectionPool().releaseConnection(accessor);
						}
					} else {
						Accessor accessor = ((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().acquireConnection();
						Connection connection = accessor.getConnection();
						Statement statement = connection.createStatement();
						try {
							statement.executeUpdate("DROP DATABASE " + instance.getDatabaseName());
						} finally {
							statement.close();
							((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().releaseConnection(accessor);
						}
					}
					EntityManager tx = factory.createEntityManager();
					tx.getTransaction().begin();
					BotInstance managed = tx.find(BotInstance.class, instance.getId());
					managed.setArchived(true);
					tx.getTransaction().commit();
				} catch (Exception exception) {
					AdminDatabase.instance().log(exception);
				}
			}
			AdminDatabase.instance().log(Level.INFO, "Archiving inactive complete", count);
		} catch (RuntimeException exception) {
			AdminDatabase.instance().log(exception);
			throw exception;
		} finally {
			if (em != null) {
				em.close();
			}
			if (postgres != null) {
				postgres.close();
			}
			if (factory != null) {
				factory.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void migrate4() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
		//properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("botlibre", properties);
		EntityManager em = factory.createEntityManager();
		List<BotInstance> instances = em.createQuery("Select p from BotInstance p").getResultList();
		AdminDatabase.instance().log(Level.INFO, "Migrating", instances.size());
		int count = 0;
		for (BotInstance instance : instances) {
			count++;
			try {
				AdminDatabase.instance().log(Level.INFO, "Migrating", count, instance.getName());
				Bot bot = connectInstance(instance);
				TextEntry sense = bot.awareness().getSense(TextEntry.class);
				if (sense != null) {
					TextInput textInput = new TextInput("ping", false, false);
					sense.input(textInput);
					long start = System.currentTimeMillis();
					while (!bot.memory().getActiveMemory().isEmpty()) {
						if ((System.currentTimeMillis() - start) > 60000) {
							break;
						}
						Thread.sleep(1000);
					}
				}
				bot.shutdown();
			} catch (Exception exception) {
				AdminDatabase.instance().log(exception);
			}
		}
		AdminDatabase.instance().log(Level.INFO, "Migration complete", count);		
	}

	public void migrate3() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "fine");
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("botlibre", properties);
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		try {
			em.createNativeQuery("Update ChatChannel set creator_userid = (Select t2.admins_userid from CHAT_ADMINS t2 where t2.chatchannel_id = id)").executeUpdate();
			em.createNativeQuery("Update Forum set creator_userid = (Select t2.admins_userid from forum_ADMINS t2 where t2.forum_id = id)").executeUpdate();
			em.createNativeQuery("Update BotInstance set creator_userid = (Select t2.admins_userid from PANODRAINSTANCE_ADMINS t2 where t2.instances_id = id)").executeUpdate();
			em.getTransaction().commit();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
			factory.close();
		}
	}


	@SuppressWarnings("unchecked")
	public void migrate2() {
		EntityManagerFactory sourceFactory = Persistence.createEntityManagerFactory("migration");
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, Site.DATABASEPASSWORD);
		properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.DROP_AND_CREATE);
		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "fine");
		EntityManagerFactory targetFactory = Persistence.createEntityManagerFactory("botlibre", properties);
		EntityManager source = sourceFactory.createEntityManager();
		EntityManager target = targetFactory.createEntityManager();
		target.getTransaction().begin();
		try {
			Domain domain = new Domain(Site.DOMAIN);
			target.persist(domain);
			
			List<Tag> tags = source.createQuery("Select t from Tag t").getResultList();
			for (Tag tag : tags) {
				tag.setDomain(domain);
				target.persist(tag);
			}
			
			List<User> users = source.createQuery("Select t from User t").getResultList();
			for (User user : users) {
				if (user.getCreationDate() == null) {
					user.setCreationDate(new Date());
				}
				target.persist(user);
			}
			
			List<AvatarImage> avatars = source.createQuery("Select t from AvatarImage t").getResultList();
			for (AvatarImage avatar : avatars) {
				avatar.setDomain(domain);
				target.persist(avatar);
			}
			
			List<BotInstance> instances = source.createQuery("Select p from BotInstance p").getResultList();
			for (BotInstance instance : instances) {
				if (instance.getCreationDate() == null) {
					instance.setCreationDate(new Date());
				}
				instance.setDomain(domain);
				target.persist(instance);
			}
			
			target.getTransaction().commit();
			target.getTransaction().begin();
			Query query = target.createNativeQuery("Update Tag t set count = (Select count(p) from BotInstance p join PANODRAINSTANCE_TAGS pt on (pt.BotInstance_id = p.id) join Tag t2 on (pt.tags_id = t2.id) where p.domain_id = ? and t2.id = t.id) where t.domain_id = ?");
			query.setParameter(1, domain.getId());
			query.setParameter(2, domain.getId());
			query.executeUpdate();
			query = target.createQuery("Delete from Tag t where t.count = 0 and t.domain = :domain");
			query.setParameter("domain", domain);
			query.executeUpdate();
			target.getTransaction().commit();
			
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (target.getTransaction().isActive()) {
				target.getTransaction().rollback();
			}
			source.close();
			target.close();
			sourceFactory.close();
			targetFactory.close();
		}
	}
}
