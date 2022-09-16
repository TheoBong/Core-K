package com.bongbong.core.utils;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Webhook {
    @SuppressWarnings("unchecked")
    public static void sendWebhook( String content, JSONArray embeds) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("content", content);
            obj.put("embeds", embeds);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            Request request = new Request.Builder()
                    .url("https://discordapp.com/api/webhooks/987407822482464778/Z9TxYt-lrrwvEaZW_dhUyq_GG2V5hUJ7PpAXwjPWV9F_LYah-ebDtmi4WMqD2K35drOa")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
