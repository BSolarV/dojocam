package com.pinneapple.dojocam_app;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Apical {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    final OkHttpClient client = new OkHttpClient();
    FirebaseUser user_email = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseAuth userhaciachat = FirebaseAuth.getInstance();
    String nombre = user_email.getDisplayName();

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
        return "no paso";
    }


    String JSONcrearusuario(String player1){
        return "{\"uid\":\""+player1+"\",\"name\":\""+nombre+"\"}";
    }

    String JSONgrupocreado(String player1, String player2) {
        String data = "{\"guid\":\""+player1+player2+"\",\"name\":\""+nombre+" - "+player2+"\",\"type\":\"private\"}";
        return data;
    }

    String JSONusers2add(String player1, String player2) {
        String data = "{\"participants\":[\""+player1+"\",\""+player2+"\"]}";
        System.out.println(data);
        return data;
    }
    public static void crearusuario(String args) throws IOException{
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    Apical example = new Apical();
                    String user2add = example.JSONcrearusuario(args);
                    Log.d("Crear usuario", user2add+"SE AGREGO ESE USUSAARI");
                    String response_usercreated = example.post("https://1985642356a8baff.api-us.cometchat.io/v3/groups", user2add);
                    System.out.println(response_usercreated);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static void sendpost(String[] args) throws IOException{
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

    public static void main(String args) throws IOException {
    }
}