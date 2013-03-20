package util;

import play.Configuration;
import play.Logger;

/**
 * General class for helper functions, connecting to MongoDB
 * 
 * @author msg
 *
 */
public class AbstractMongoBL {

	/**
	 * 
	 * @return The configured MongoDB hostname or 'localhost'
	 */
	public String getMongoHost() {
		return getStringConfigValue("mongo.host", "localhost");
	}
	
	/**
	 * 
	 * @return The configured MongoDB port or 27017
	 */
	public Integer getMongoPort() {
		return getIntegerConfigValue("mongo.port", 27017);
	}
	
	/**
	 * 
	 * @return The configured MongoDB database or 'playDb'
	 */
	public String getMongoDB() {
		return getStringConfigValue("mongo.database", "playDb");
	}

	/**
	 * Resolves the given <code>key</code> and returns its value or <code>defaultValue</code>
	 * @param key The key to look up
	 * @param defaultValue The default value if nothing is set
	 * @return
	 */
	protected String getStringConfigValue(String key, String defaultValue) {
		
		final String result = Configuration.root().getString(key, defaultValue);
		if(Logger.isTraceEnabled()) {
			Logger.trace("Config entry '" + key + "' value: '" + result + "'");
		}
		
		return result;
	}
	
	/**
	 * Resolves the given <code>key</code> and returns its value or <code>defaultValue</code>
	 * @param key The key to look up
	 * @param defaultValue The default value if nothing is set
	 * @return
	 */
	protected Integer getIntegerConfigValue(String key, Integer defaultValue) {
		
		final int result = Configuration.root().getInt(key, defaultValue);
		if(Logger.isTraceEnabled()) {
			Logger.trace("Config entry '" + key + "' value: '" + result + "'");
		}
		
		return result;
	}
	
	
	/**
	 * 
	 * @return The configured MongoDB collection or 'polls'
	 */
	public String getMongoPollsCollection() {
		return getStringConfigValue("mongo.pollsCollection", "polls");
	}
	
	/**
	 * 
	 * @return The configured MongoDB collection or 'users'
	 */
	public String getMongoUsersCollection() {
		return getStringConfigValue("mongo.usersCollection", "users");
	}
}
