package cc.kitpvp.core.networking.mongo;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

public interface MongoCollectionResult {
    void call(MongoCollection<Document> collection);
}
