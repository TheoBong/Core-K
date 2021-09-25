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
                    .url("https://discord.com/api/webhooks/887518026608566342/RgE2kKXuHBqZFn1jdR1CqCLs0nBA61TEO3Vz-Rqi5aNQr7N6tww7lnUwDlfQhwDkw9Pp")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
