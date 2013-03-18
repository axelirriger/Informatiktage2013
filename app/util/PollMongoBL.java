
package util;

import java.net.UnknownHostException;
import java.util.ArrayList;
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

public class PollMongoBL {
    private DBCollection collection;
    public PollMongoBL() {
        try {
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            DB db = mongoClient.getDB("playDb");
            collection = db.getCollection("polls");
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    public DBCollection getCollection() {
        return collection;
    }
    public List<PollMongoEntity> getAllPolls() {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> PollMongoBL.getAllPolls()");
        }
        final DBCursor cursor = getCollection().find();
        final List<PollMongoEntity> allPolls = new ArrayList<PollMongoEntity>();
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
    public void savePoll(final PollMongoEntity pollEntity) {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> PollMongoBL.savePoll(PollMongoEntity)");
        }
        final DBObject toSave = buildDBObjectFromEntity(pollEntity);
        getCollection().insert(toSave);
        if (Logger.isTraceEnabled()) {
            Logger.trace(" Poll saved:");
            Logger.trace(" Poll Name: " + pollEntity.pollName);
            Logger.trace(" Poll Description: " + pollEntity.pollDescription);
        }
        if (Logger.isDebugEnabled()) {
            Logger.debug("< PollMongoBL.savePoll(PollMongoEntity)");
        }
    }
    public PollMongoEntity loadPoll(final String pollName) {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> PollMongoBL.loadPoll(String)");
        }
        DBObject object = new BasicDBObject();
        object.put("_id", pollName);
        long start = System.currentTimeMillis();
        DBObject cursor = getCollection().findOne(object);
        long end = System.currentTimeMillis();
        PollMongoEntity entity = null;
        if (cursor != null) {
            entity = buildPollEntity(cursor);
        }
        if (Logger.isDebugEnabled()) {
            Logger.debug("Poll with name '" + pollName + "' loaded in " + (end - start) + "ms");
        }
        if (Logger.isTraceEnabled()) {
            Logger.trace("Loaded Poll:");
            Logger.trace(" Poll Name: " + entity.pollName);
            Logger.trace(" Poll Description: " + entity.pollDescription);
            Logger.trace("Poll Options:");
            if (entity.optionsName != null && entity.optionsName.size() > 0) {
                for (int i = 0; i < entity.optionsName.size(); i++) {
                    Logger.trace(" Option[" + i + "]: " + entity.optionsName.get(i));
                }
            }
            else {
                Logger.trace("No option for this poll...");
            }
            if (entity.results != null && entity.results.size() > 0) {
                Logger.trace(" Number of Entries: " + entity.results.size());
            }
        }
        if (Logger.isDebugEnabled()) {
            Logger.debug("< PollMongoBL.loadPoll(String)");
        }
        return entity;
    }
    public void addEntryToPoll(final String pollName, final PollMongoResultEntity entryEntity) {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> PollMongoBL.addEntryToPoll(String, PollMongoResultEntity)");
        }
        final DBObject query = new BasicDBObject();
        //query.put("name", pollName);
        query.put("_id", pollName);
        final DBObject cursor = getCollection().findOne(query);
        if (cursor != null) {
            BasicDBObject object = (BasicDBObject) cursor;
            addMissingFalseValues(entryEntity, object);
            BasicDBList resultsList = (BasicDBList) object.get("results");
            BasicDBObject entryToSave = buildEntryFromEntity(entryEntity);
            resultsList.add(entryToSave);
            getCollection().update(query, object);
            if (Logger.isTraceEnabled()) {
                Logger.trace(" New Entry added to Poll with Name " + pollName);
                Logger.trace(" Entry Values:");
                Logger.trace(" Added By: " + entryEntity.participantName);
                Logger.trace(" Participant email: " + entryEntity.email);
            }
        }
        //TODO add poll to completedPolls
        //UserMongoBL.addPollToCompletedPolls(entryEntity.participantName, pollName);
        if (Logger.isDebugEnabled()) {
            Logger.debug("< PollMongoBL.addEntryToPoll(String, PollMongoResultEntity)");
        }
    }
    private void addMissingFalseValues(PollMongoResultEntity entryEntity, BasicDBObject object) {
        if (Logger.isDebugEnabled()) {
            Logger
                .debug("> PollMongoBL.addMissingFalseValues(PollMongoResultEntity, BasicDBObject)");
        }
        BasicDBList optionNList = (BasicDBList) object.get("optionNames");
        if (entryEntity.optionValues != null && entryEntity.optionValues.size() > 0) {
            for (int i = 0; i < entryEntity.optionValues.size(); i++) {
                if (entryEntity.optionValues.get(i) == null) {
                    entryEntity.optionValues.set(i, Boolean.FALSE);
                }
            }
        }
        if (optionNList != null && optionNList.size() > 0
            && optionNList.size() > entryEntity.optionValues.size()) {
            int sizeDifference = optionNList.size() - entryEntity.optionValues.size();
            for (int i = 0; i < sizeDifference; i++) {
                entryEntity.optionValues.add(Boolean.FALSE);
            }
        }
        if (Logger.isDebugEnabled()) {
            Logger
                .debug("< PollMongoBL.addMissingFalseValues(PollMongoResultEntity, BasicDBObject)");
        }
    }
    private BasicDBObject buildEntryFromEntity(PollMongoResultEntity resultEntity) {
        BasicDBObject element = new BasicDBObject();
        //element.put("name", resultEntity.participantName);
        element.put("_id", resultEntity.participantName);
        element.put("email", resultEntity.email);
        BasicDBList optionsList = new BasicDBList();
        if (resultEntity.optionValues != null && resultEntity.optionValues.size() > 0) {
            for (int i = 0; i < resultEntity.optionValues.size(); i++) {
                optionsList.add(resultEntity.optionValues.get(i));
            }
        }
        element.put("optionValues", optionsList);
        return element;
    }
    private PollMongoEntity buildPollEntity(DBObject object) {
        PollMongoEntity entity = new PollMongoEntity();
        //entity.pollName = (String) object.get("name");
        entity.pollName = (String) object.get("_id");
        entity.pollDescription = (String) object.get("beschreibung");
        entity.creator = (String) object.get("creator");
        BasicDBList optionNamesList = (BasicDBList) object.get("optionNames");
        if (optionNamesList != null && optionNamesList.size() > 0) {
            for (int i = 0; i < optionNamesList.size(); i++) {
                entity.optionsName.add((String) optionNamesList.get(i));
            }
        }
        BasicDBList results = (BasicDBList) object.get("results");
        List<PollMongoResultEntity> resultEntities = new ArrayList<PollMongoResultEntity>();
        if (results != null) {
            for (Object res : results) {
                if (res instanceof DBObject) {
                    DBObject dbRes = (DBObject) res;
                    PollMongoResultEntity resultEntity = new PollMongoResultEntity();
                    //resultEntity.participantName = (String) dbRes.get("name");
                    resultEntity.participantName = (String) dbRes.get("_id");
                    resultEntity.email = (String) dbRes.get("email");
                    BasicDBList optionsList = (BasicDBList) dbRes.get("optionValues");
                    if (optionsList != null && optionsList.size() > 0) {
                        for (int i = 0; i < optionsList.size(); i++) {
                            resultEntity.optionValues.add((Boolean) optionsList.get(i));
                        }
                    }
                    resultEntities.add(resultEntity);
                }
            }
        }
        entity.results = resultEntities;
        return entity;
    }
    private BasicDBObject buildDBObjectFromEntity(PollMongoEntity pollEntity) {
        BasicDBObject object = new BasicDBObject();
        //object.put("name", pollEntity.pollName);
        object.put("_id", pollEntity.pollName);
        object.put("beschreibung", pollEntity.pollDescription);
        object.put("creator", pollEntity.creator);
        BasicDBList optionsList = new BasicDBList();
        for (String option : pollEntity.optionsName) {
            optionsList.add(option);
        }
        object.put("optionNames", optionsList);
        BasicDBList resultsList = new BasicDBList();
        for (PollMongoResultEntity resultEntity : pollEntity.results) {
            BasicDBObject element = buildEntryFromEntity(resultEntity);
            resultsList.add(element);
        }
        object.put("results", resultsList);
        return object;
    }
    public List<PollMongoEntity> loadCreatedPolls(String username) {
        BasicDBObject objectFinder = new BasicDBObject();
        objectFinder.put("creator", username);
        DBCursor cursor = getCollection().find(objectFinder);
        List<PollMongoEntity> createdPollList = new ArrayList<PollMongoEntity>();
        while (cursor.hasNext()) {
            BasicDBObject object = (BasicDBObject) cursor.next();
            createdPollList.add(buildPollEntity(object));
        }
        return createdPollList;
    }
}
