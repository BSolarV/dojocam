package com.pinneapple.dojocam_app;

import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Apical {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    final OkHttpClient client = new OkHttpClient();

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apiKey", "3ed98e35abec92cfeb5391518de9c9fee73bfb4d")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            return response.body().string();
        }
        catch (Exception e){
            Log.d("das", String.valueOf(e));
        }
        return "se hoizopozozoz";
    }

    String JSONgrupocreado(String player1, String player2) {
        String data = "{\"guid\":\""+player1+player2+"\",\"name\":\""+player1+player2+"\",\"type\":\"private\"}";
        return data;
    }

    String JSONusers2add(String player1, String player2) {
        String data = "{\"participants\":[\""+player1+"\",\""+player2+"\"]}";
        System.out.println(data);
        return data;
    }

    public static void main(String[] args) throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    Apical example = new Apical();
                    String json_group = example.JSONgrupocreado(args[0], args[1]);
                    String json_users = example.JSONusers2add(args[0], args[1]);
                    Log.d("WEEEEEEEEEEEE", json_group+"-----------------------"+json_users);
                    String response_group = example.post("https://1985642356a8baff.api-us.cometchat.io/v3/groups", json_group);
                    String response_users = example.post("https://1985642356a8baff.api-us.cometchat.io/v3/groups/"+args[0]+args[1]+"/members", json_users);
                    System.out.println(response_group);
                    System.out.println(response_users);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}