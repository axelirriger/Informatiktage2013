package util;

import play.Configuration;

public class MongoBL {

	/**
	 * 
	 * @return The configured MongoDB hostname or 'localhost'
	 */
	public String getMongoHost() {
		return Configuration.root().getString("mongo.host", "localhost");
	}
	
	/**
	 * 
	 * @return The configured MongoDB port or 27017
	 */
	public Integer getMongoPort() {
		return Configuration.root().getInt("mongo.port", 27017);
	}
	
	/**
	 * 
	 * @return The configured MongoDB database or 'playDb'
	 */
	public String getMongoDB() {
		return Configuration.root().getString("mongo.database", "playDb");
	}
	
	/**
	 * 
	 * @return The configured MongoDB collection or 'polls'
	 */
	public String getMongoPollsCollection() {
		return Configuration.root().getString("mongo.pollsCollection", "polls");
	}
	
	/**
	 * 
	 * @return The configured MongoDB collection or 'users'
	 */
	public String getMongoUsersCollection() {
		return Configuration.root().getString("mongo.usersCollection", "users");
	}
}
