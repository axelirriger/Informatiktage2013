package util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.PollMongoEntity;
import models.PollMongoResultEntity;
import play.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * Class to handle Poll related data
 * 
 * @author msg
 *
 */
public class PollMongoBL extends AbstractMongoBL {

	/**
	 * The collection to access polls
	 */
	private DBCollection collection;

	public PollMongoBL() {
		if(Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.PollMongoBL()");
		}

		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(getMongoHost(), getMongoPort());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}

		DB db = mongoClient.getDB(getMongoDB());
		collection = db.getCollection(getMongoPollsCollection());

		if(Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.PollMongoBL()");
		}
	}

	/**
	 * 
	 * @return The MongoDB collection
	 */
	private DBCollection getCollection() {
		return collection;
	}

	/**
	 * 
	 * @return A list of all registered polls
	 */
	public List<PollMongoEntity> getAllPolls() {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.getAllPolls()");
		}

		final List<PollMongoEntity> allPolls = new ArrayList<PollMongoEntity>();

		// Query all polls
		final DBCursor cursor = getCollection().find();
		while (cursor.hasNext()) {
			final DBObject object = cursor.next();
			allPolls.add(buildPollEntity(object));
		}
		cursor.close();

		if (Logger.isTraceEnabled()) {
			Logger.trace(" Loaded " + allPolls.size() + " Polls");
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.getAllPolls()");
		}
		return allPolls;
	}

	/**
	 * Saves the given poll to MongoDB
	 * 
	 * @param pollEntity
	 */
	public void savePoll(final PollMongoEntity pollEntity) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.savePoll(PollMongoEntity)");
		}

		final DBObject toSave = buildDBObjectFromEntity(pollEntity);
		getCollection().insert(toSave);
		
		if (Logger.isTraceEnabled()) {
			Logger.trace("Poll saved:");
			Logger.trace("Poll Name: " + pollEntity.pollName);
			Logger.trace("Poll Description: " + pollEntity.pollDescription);
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.savePoll(PollMongoEntity)");
		}
	}

	/**
	 * Loads the poll with the given ID
	 * 
	 * @param pollName The poll ID
	 * @return The poll or <code>null</code>
	 */
	public PollMongoEntity loadPoll(final String pollName) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.loadPoll(String)");
		}
		PollMongoEntity entity = null;

		final DBObject object = new BasicDBObject();
		object.put("_id", pollName);
		
		long start = System.currentTimeMillis();
		final DBObject cursor = getCollection().findOne(object);
		long end = System.currentTimeMillis();
		if (Logger.isDebugEnabled()) {
			Logger.debug("Poll with name '" + pollName + "' loaded in "
					+ (end - start) + "ms");
		}
		
		if (cursor != null) {
			entity = buildPollEntity(cursor);
			if (Logger.isTraceEnabled()) {
				Logger.trace("Loaded Poll:");
				Logger.trace(" Poll Name: " + entity.pollName);
				Logger.trace(" Poll Description: " + entity.pollDescription);
				Logger.trace("Poll Options:");
				int optionsLength = entity.optionsName.size();
				if (entity.optionsName != null && optionsLength > 0) {
					for (int i = 0; i < optionsLength; i++) {
						Logger.trace(" Option[" + i + "]: "
								+ entity.optionsName.get(i));
					}
				} else {
					Logger.trace("No option for this poll...");
				}
				int entrySize = entity.results.size();
				if (entity.results != null && entrySize > 0) {
					Logger.trace(" Number of Entries: " + entrySize);
				}
			}
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.loadPoll(String)");
		}
		return entity;
	}

	/**
	 * Adds an entry to the given poll
	 * 
	 * @param pollName The poll ID 
	 * @param entryEntity The entry to save
	 */
	public void addEntryToPoll(final String pollName,
			final PollMongoResultEntity entryEntity) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.addEntryToPoll(String, PollMongoResultEntity)");
		}

		final DBObject query = new BasicDBObject();
		query.put("_id", pollName);
		
		long start = System.currentTimeMillis();
		final DBObject cursor = getCollection().findOne(query);
		long end = System.currentTimeMillis();
		if(Logger.isDebugEnabled()) {
			Logger.debug("Loaded poll " + pollName + " in " + (end-start) + " msec");
		}
		
		if (cursor != null) {
			final BasicDBObject object = (BasicDBObject) cursor;
			addMissingFalseValues(entryEntity, object);
			final BasicDBList resultsList = (BasicDBList) object.get("results");
			final BasicDBObject entryToSave = buildEntryFromEntity(entryEntity);
			resultsList.add(entryToSave);
			getCollection().update(query, object);
			
			if (Logger.isTraceEnabled()) {
				Logger.trace(" New Entry added to Poll with Name " + pollName);
				Logger.trace(" Entry Values:");
				Logger.trace(" Added By: " + entryEntity.participantName);
				Logger.trace(" Participant email: " + entryEntity.email);
			}
		}

		final UserMongoBL userData = new UserMongoBL();
		userData.addPollToPollsParticipated(entryEntity.participantName, pollName);

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.addEntryToPoll(String, PollMongoResultEntity)");
		}
	}

	/**
	 * Deletes the given entry from the given poll
	 * @param pollName
	 * @param voteId
	 */
	public void deleteEntryFromPoll(final String pollName, final String voteId) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.deleteEntryToPoll(" + pollName + ", "
					+ voteId + ")");
		}
		
		final DBObject query = new BasicDBObject();
		query.put("_id", pollName);
		final DBObject cursor = getCollection().findOne(query);
		if (cursor != null) {
			final BasicDBObject object = (BasicDBObject) cursor;
			final BasicDBList resultsList = (BasicDBList) object.get("results");
			final Iterator<Object> iterator = resultsList.iterator();
			while (iterator.hasNext()) {
				final BasicDBObject item = (BasicDBObject) iterator.next();
				if (voteId.equals(item.getString("_id"))) {
					iterator.remove();
				}
			}
			getCollection().update(query, object);
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.deleteEntryToPoll(" + pollName + ", "
					+ voteId + ")");
		}
	}

	/**
	 * Updates the loaded entity so that not only set values but also not set values are found.
	 * 
	 * @param entryEntity
	 * @param object
	 */
	private void addMissingFalseValues(PollMongoResultEntity entryEntity,
			BasicDBObject object) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.addMissingFalseValues(PollMongoResultEntity, BasicDBObject)");
		}

		final BasicDBList optionNList = (BasicDBList) object.get("optionNames");
		if (entryEntity.optionValues != null
				&& entryEntity.optionValues.size() > 0) {
			for (int i = 0; i < entryEntity.optionValues.size(); i++) {
				if (entryEntity.optionValues.get(i) == null) {
					entryEntity.optionValues.set(i, Boolean.FALSE);
				}
			}
		}
		
		if (optionNList != null && optionNList.size() > 0
				&& optionNList.size() > entryEntity.optionValues.size()) {
			int sizeDifference = optionNList.size()
					- entryEntity.optionValues.size();
			for (int i = 0; i < sizeDifference; i++) {
				entryEntity.optionValues.add(Boolean.FALSE);
			}
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.addMissingFalseValues(PollMongoResultEntity, BasicDBObject)");
		}
	}

	/**
	 * Convenience method to populate a MongoDB object
	 * 
	 * @param resultEntity
	 * @return
	 */
	private BasicDBObject buildEntryFromEntity(
			PollMongoResultEntity resultEntity) {
		if(Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.buildEntryFromEntity(PollMongoResultEntity)");
		}

		final BasicDBObject element = new BasicDBObject();
		element.put("_id", resultEntity.participantName);
		element.put("email", resultEntity.email);
		final BasicDBList optionsList = new BasicDBList();
		if (resultEntity.optionValues != null
				&& resultEntity.optionValues.size() > 0) {
			for (int i = 0; i < resultEntity.optionValues.size(); i++) {
				optionsList.add(resultEntity.optionValues.get(i));
			}
		}
		element.put("optionValues", optionsList);

		if(Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.buildEntryFromEntity(PollMongoResultEntity)");
		}
		return element;
	}

	/**
	 * Convenience method to convert a MongoDB object to a DTO
	 * @param object
	 * @return
	 */
	private PollMongoEntity buildPollEntity(final DBObject object) {
		if(Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.buildPollEntity(DBObject)");
		}

		final PollMongoEntity entity = new PollMongoEntity();
		entity.pollName = (String) object.get("_id");
		entity.pollDescription = (String) object.get("beschreibung");
		entity.creator = (String) object.get("creator");
		final BasicDBList optionNamesList = (BasicDBList) object.get("optionNames");
		if (optionNamesList != null && optionNamesList.size() > 0) {
			for (int i = 0; i < optionNamesList.size(); i++) {
				entity.optionsName.add((String) optionNamesList.get(i));
			}
		}
		final BasicDBList results = (BasicDBList) object.get("results");
		final List<PollMongoResultEntity> resultEntities = new ArrayList<PollMongoResultEntity>();
		if (results != null) {
			for (final Object res : results) {
				if (res instanceof DBObject) {
					final DBObject dbRes = (DBObject) res;
					final PollMongoResultEntity resultEntity = new PollMongoResultEntity();
					resultEntity.participantName = (String) dbRes.get("_id");
					resultEntity.email = (String) dbRes.get("email");
					final BasicDBList optionsList = (BasicDBList) dbRes
							.get("optionValues");
					if (optionsList != null && optionsList.size() > 0) {
						for (int i = 0; i < optionsList.size(); i++) {
							resultEntity.optionValues.add((Boolean) optionsList
									.get(i));
						}
					}
					resultEntities.add(resultEntity);
				}
			}
		}
		entity.results = resultEntities;

		if(Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.buildPollEntity(DBObject)");
		}
		return entity;
	}

	private BasicDBObject buildDBObjectFromEntity(PollMongoEntity pollEntity) {
		if(Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.buildDBObjectFromEntity(PollMongoEntity)");
		}

		final BasicDBObject object = new BasicDBObject();
		object.put("_id", pollEntity.pollName);
		object.put("beschreibung", pollEntity.pollDescription);
		object.put("creator", pollEntity.creator);
		final BasicDBList optionsList = new BasicDBList();
		for (final String option : pollEntity.optionsName) {
			optionsList.add(option);
		}
		object.put("optionNames", optionsList);
		final BasicDBList resultsList = new BasicDBList();
		for (final PollMongoResultEntity resultEntity : pollEntity.results) {
			final BasicDBObject element = buildEntryFromEntity(resultEntity);
			resultsList.add(element);
		}
		object.put("results", resultsList);

		if(Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.buildDBObjectFromEntity(PollMongoEntity)");
		}
		return object;
	}

	public List<PollMongoEntity> loadCreatedPolls(String username) {
		if(Logger.isDebugEnabled()) {
			Logger.debug("> PollMongoBL.loadCreatedPolls(String)");
		}

		final BasicDBObject objectFinder = new BasicDBObject();
		objectFinder.put("creator", username);
		final DBCursor cursor = getCollection().find(objectFinder);
		final List<PollMongoEntity> createdPollList = new ArrayList<PollMongoEntity>();
		while (cursor.hasNext()) {
			final BasicDBObject object = (BasicDBObject) cursor.next();
			createdPollList.add(buildPollEntity(object));
		}

		if(Logger.isDebugEnabled()) {
			Logger.debug("< PollMongoBL.loadCreatedPolls(String)");
		}
		return createdPollList;
	}
}
