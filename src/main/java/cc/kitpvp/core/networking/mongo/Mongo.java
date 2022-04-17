package cc.kitpvp.core.networking.mongo;

import cc.kitpvp.core.Core;
import cc.kitpvp.core.utils.ThreadUtil;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.UuidRepresentation;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mongo {
    private final Core core;
    private final MongoDatabase mongoDatabase;

    public Mongo(Core core) {
        this.core = core;

        MongoClient mongoClient;
        if (core.getConfig().getBoolean("networking.mongo.localhost")) {
            mongoClient = MongoClients.create(MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD).build());
            mongoDatabase = mongoClient.getDatabase(core.getConfig().getString("networking.mongo.db"));
        } else {
            MongoClientSettings mcs = MongoClientSettings.builder()
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .applyConnectionString(new ConnectionString(core.getConfig().getString("networking.mongo.uri")))
                    .build();

            mongoClient = MongoClients.create(mcs);
            mongoDatabase = mongoClient.getDatabase(core.getConfig().getString("networking.mongo.db"));
        }
    }


    public void deleteDocument(boolean async, String collectionName, Object id) {
        ThreadUtil.runTask(async, core, () -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
            Document document = new Document("_id", id);

            collection.deleteMany(document);
        });
    }

    public void getDocument(boolean async, String collectionName, Object id, MongoResult mongoResult) {
        ThreadUtil.runTask(async, core, () -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            if (collection.find(Filters.eq("_id", id)).iterator().hasNext()) {
                mongoResult.call(collection.find(Filters.eq("_id", id)).first());
            } else {
                mongoResult.call(null);
            }
        });
    }

    public void massUpdate(boolean async, MongoUpdate mongoUpdate) {
        massUpdate(async, mongoUpdate.getCollectionName(), mongoUpdate.getId(), mongoUpdate.getUpdate());
    }

    public void massUpdate(boolean async, String collectionName, Object id, Map<String, Object> updates) throws LinkageError {
        ThreadUtil.runTask(async, core, () -> {
            final MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            Document document = collection.find(new Document("_id", id)).first();
            if(document == null) {
                collection.insertOne(new Document("_id", id));
            }

            updates.forEach((key, value) -> collection.updateOne(Filters.eq("_id", id), Updates.set(key, value)));
        });
    }

    public void createCollection(boolean async, String collectionName) {
        ThreadUtil.runTask(async, core, () -> {
            AtomicBoolean exists = new AtomicBoolean(false);
            mongoDatabase.listCollectionNames().forEach(s -> {
                if(s.equals(collectionName)) {
                    exists.set(true);
                }
            });

            if(!exists.get()) {
                mongoDatabase.createCollection(collectionName);
            }
        });
    }

    public void getCollectionIterable(boolean async, String collectionName, MongoIterableResult mir) {
        ThreadUtil.runTask(async, core, ()-> mir.call(mongoDatabase.getCollection(collectionName).find()));
    }
}
