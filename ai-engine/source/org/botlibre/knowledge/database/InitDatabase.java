package org.botlibre.knowledge.database;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.jpa.JpaEntityManagerFactory;


public class InitDatabase  {
	public static void main(String[] args) {
		new InitDatabase().initDatabase();
		System.exit(0);
	}

	public void initDatabase() {
		Map<String, String> properties = new HashMap<String, String>();
		//properties.put(PersistenceUnitProperties.LOGGING_LEVEL, "FINE");
		EntityManagerFactory postgres = Persistence.createEntityManagerFactory("Bot", properties);
		EntityManagerFactory factory = null;
		Accessor accessor = null;
		Statement statement = null;
		try {
			System.out.println("Creating database");
			try {
				accessor = ((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().acquireConnection();
				Connection connection = accessor.getConnection();
				statement = connection.createStatement();
				try {
					connection.createStatement().executeUpdate("CREATE DATABASE CACHE");
				} catch (Exception exception) {
					exception.printStackTrace();					
                }
                try {
                    connection.createStatement().executeUpdate("CREATE DATABASE TEST");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                try {
                    connection.createStatement().executeUpdate("CREATE DATABASE botlibre_bots");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
				((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().releaseConnection(accessor);
				
				properties = new HashMap<String, String>();
				properties.put(PersistenceUnitProperties.JDBC_URL, DatabaseMemory.DATABASE_URL);
				properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
				factory = Persistence.createEntityManagerFactory("Bot", properties);
				
				accessor = ((JpaEntityManagerFactory)factory).getServerSession().getReadConnectionPool().acquireConnection();
				connection = accessor.getConnection();
				statement = connection.createStatement();
				try {
					statement.executeUpdate("CREATE EXTENSION dblink schema public");
				} catch (Exception exception) {
					exception.printStackTrace();					
				}
				((JpaEntityManagerFactory)postgres).getServerSession().getReadConnectionPool().releaseConnection(accessor);
				
				factory.createEntityManager().close();
				factory.close();
                
                properties = new HashMap<String, String>();
                properties.put(PersistenceUnitProperties.JDBC_URL, DatabaseMemory.DATABASE_URL_PREFIX + "cache");
                properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
                factory = Persistence.createEntityManagerFactory("Bot", properties);
                
                factory.createEntityManager().close();
                factory.close();
                
                properties = new HashMap<String, String>();
                properties.put(PersistenceUnitProperties.JDBC_URL, DatabaseMemory.DATABASE_URL_PREFIX + "test");
                properties.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_OR_EXTEND);
                factory = Persistence.createEntityManagerFactory("Bot", properties);
                
                factory.createEntityManager().close();
                factory.close();
				
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			
			postgres.close();
			System.out.println("Init complete");
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
			exception.printStackTrace();
			throw exception;
		}
	}
}
