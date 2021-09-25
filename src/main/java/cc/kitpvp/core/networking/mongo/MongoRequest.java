package cc.kitpvp.core.networking.mongo;

import cc.kitpvp.core.Core;

import java.util.HashMap;

public class MongoRequest {
    private final String collectionName;
    private final Object id;
    private final HashMap<String, Object> updatePairs = new HashMap<>();

    private MongoRequest(final String collectionName, final Object id) {
        this.collectionName = collectionName;
        this.id = id;
    }

    public static MongoRequest newRequest(String collectionName, Object id) {
        return new MongoRequest(collectionName, id);
    }

    public MongoRequest put(String key, Object value) {
        this.updatePairs.put(key, value);
        return this;
    }

    public void run(boolean async) {
        Core.get().getMongo().massUpdate(async, this.collectionName, this.id, this.updatePairs);
    }
}