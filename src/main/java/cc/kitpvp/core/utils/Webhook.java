package cc.kitpvp.core.utils;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Webhook {
    @SuppressWarnings("unchecked")
    public static void sendWebhook(String username, String avatar_url, String content, JSONArray embeds) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("username", username);
            obj.put("avatar_url", avatar_url);
            obj.put("content", content);
            obj.put("embeds", embeds);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            Request request = new Request.Builder()
                    .url("https://discord.com/api/webhooks/923027883067330620/ObC1mAQyRi0XpJgsTY4FVWkyljEwq0TmTOJ2kql25iyfdK1bfnzPZvKN_JqBSzNnAa-D")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
