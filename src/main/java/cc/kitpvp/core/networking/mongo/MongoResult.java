package cc.kitpvp.core.networking.mongo;

import org.bson.Document;

public interface MongoResult {
    void call(Document document);
}
