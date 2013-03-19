package util;

import java.net.UnknownHostException;
import java.util.List;
import models.UserMongoEntity;
import play.Logger;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class UserMongoBL extends MongoBL {
	private DBCollection collection;

	public UserMongoBL() {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(getMongoHost(), getMongoPort());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

		DB db = mongoClient.getDB(getMongoDB());
		collection = db.getCollection(getMongoUsersCollection());
	}

	public DBCollection getCollection() {
		return collection;
	}

	public void saveUser(final UserMongoEntity user) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> UserMongoBL.saveUser(UserMongoEntity)");
		}
		
		final DBObject objectToSave = buildDBObjectFromEntity(user, true);
		getCollection().insert(objectToSave);
		
		if (Logger.isDebugEnabled()) {
			Logger.debug("< UserMongoBL.saveUser(UserMongoEntity)");
		}
	}

	public UserMongoEntity loadUser(UserMongoEntity user) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> UserMongoBL.loadUser(UserMongoEntity)");
		}
		
		UserMongoEntity result = null;
		final DBObject objectToFind = buildDBObjectFromEntity(user);
		final DBObject obj = getCollection().findOne(objectToFind);
		if (obj != null) {
			result = buildEntityFromDBObject(obj);
		}
		
		if (Logger.isDebugEnabled()) {
			Logger.debug("< UserMongoBL.loadUser(UserMongoEntity)");
		}
		return result;
	}

	public void addPollToCompletedPolls(String username, String pollName) {
		BasicDBObject objFind = new BasicDBObject();
		objFind.put("_id", username);
		DBCursor cursor = getCollection().find(objFind);
		if (cursor.hasNext()) {
			BasicDBObject next = (BasicDBObject) cursor.next();
			BasicDBList completedList = (BasicDBList) next
					.get("completedPolls");
			completedList.add(pollName);
			next.put("completedPolls", completedList);
			getCollection().update(objFind, next);
		}
	}

	private UserMongoEntity buildEntityFromDBObject(final DBObject result) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> UserMongoBL.buildEntityFromDBObject(UserMongoEntity)");
		}
		
		final UserMongoEntity entity = new UserMongoEntity();
		entity.username = (String) result.get("_id");
		entity.password = (String) result.get("password");
		entity.email = (String) result.get("email");
		if (Logger.isDebugEnabled()) {
			Logger.debug("< UserMongoBL.buildEntityFromDBObject(UserMongoEntity)");
		}
		BasicDBList completedList = (BasicDBList) result.get("completedPolls");
		if (completedList != null && completedList.size() > 0) {
			for (int i = 0; i < completedList.size(); i++) {
				entity.completedPolls.add((String) completedList.get(i));
			}
		}
		
		return entity;
	}

	private DBObject buildDBObjectFromEntity(UserMongoEntity user,
			boolean withCompletedList) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> UserMongoBL.buildDBObjectFromEntity(UserMongoEntity)");
		}
		
		DBObject object = new BasicDBObject();
		object.put("_id", user.username);
		if (user.password != null) {
			object.put("password", user.password);
		}
		if (user.email != null) {
			object.put("email", user.email);
		}
		if (withCompletedList) {
			BasicDBList completedList = new BasicDBList();
			if (user.completedPolls != null && user.completedPolls.size() > 0) {
				for (String pollStr : user.completedPolls) {
					completedList.add(pollStr);
				}
			}
			object.put("completedPolls", completedList);
		}
		
		if (Logger.isDebugEnabled()) {
			Logger.debug("< UserMongoBL.buildDBObjectFromEntity(UserMongoEntity)");
		}
		return object;
	}

	private DBObject buildDBObjectFromEntity(UserMongoEntity user) {
		return buildDBObjectFromEntity(user, false);
	}

	public List<String> loadCompletedPollsByUser(String username) {
		UserMongoEntity entity = loadUser(new UserMongoEntity(username, null,
				null));
		return entity.completedPolls;
	}
}
