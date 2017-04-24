package com.tikinokys.dontletmeplay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;

public class UnblockActivity extends AppCompatActivity {

    SharedPreferences sPref;
    String token = "";

    private class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        StringBuffer buffer = new StringBuffer();

        Boolean status = false;

        int friendsLength;

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject dataJsonObj = null;
            try {
                URL url = new URL("https://dlmp.herokuapp.com/api/users/getFriendInfo/" + loginFriend + "?token=" + token);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

                dataJsonObj = new JSONObject(resultJson);
                status = dataJsonObj.getBoolean("status");
                if(status) {
                    JSONObject data = dataJsonObj.getJSONObject("data");
                    friendsLength = data.getInt("friendsLength");
                    JSONArray arrayOfFriendsJSON = data.getJSONArray("friends");
                    blockedFriendsArray = new BlockedFriend[friendsLength];
                    if(friendsLength>0) {
                        for (int i = 0; i < friendsLength; i++) {
                            JSONObject currentFriendJSON = (JSONObject) arrayOfFriendsJSON.get(i);
                            long a = currentFriendJSON.getLong("block_expires")*1000;
                            Timestamp stamp = new Timestamp(a);
                            Date date = new Date(stamp.getTime());
                            String s = String.valueOf(DateFormat.format("dd-MM-yyyy (HH:mm)", date));
                            blockedFriendsArray[i] = new BlockedFriend(currentFriendJSON.getString("username"), s);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String result){
            if(status){
                initFillIn();
            }else{
            }
        }
    }

    private class ParseTask1 extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        StringBuffer buffer = new StringBuffer();

        Boolean status = false;

        int friendsLength;

        @Override
        protected void onPreExecute(){

        }

        @Override
        protected String doInBackground(Void... params) {
            JSONObject dataJsonObj = null;
            try {
                URL url = new URL("https://dlmp.herokuapp.com/api/users/getBlockedFriends?token=" + token);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

                dataJsonObj = new JSONObject(resultJson);
                status = dataJsonObj.getBoolean("status");
                if(status) {
                    JSONObject data = dataJsonObj.getJSONObject("data");
                    friendsLength = data.getInt("friendsLength");
                    JSONArray arrayOfFriendsJSON = data.getJSONArray("friends");
                    blockedFriendsArray = new BlockedFriend[friendsLength];
                    if(friendsLength>0) {
                        for (int i = 0; i < friendsLength; i++) {
                            JSONObject currentFriendJSON = (JSONObject) arrayOfFriendsJSON.get(i);
                            long a = currentFriendJSON.getLong("block_expires")*1000;
                            Timestamp stamp = new Timestamp(a);
                            Date date = new Date(stamp.getTime());
                            String s = String.valueOf(DateFormat.format("dd-MM-yyyy (HH:mm)", date));
                            blockedFriendsArray[i] = new BlockedFriend(currentFriendJSON.getString("username"), s);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String result){
            if(status){
                initFillIn();
            }else{
            }
        }
    }

    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.menu_unblock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unblock);
    }
}
